package kr.hhplus.be.server.support.aop

import kr.hhplus.be.server.infrastructure.redis.RedisRepository
import kr.hhplus.be.server.support.cache.CacheKeyGenerator
import kr.hhplus.be.server.support.cache.InMemoryCache
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.slf4j.LoggerFactory
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.lang.reflect.ParameterizedType
import java.time.Duration
import java.time.LocalDate

@Aspect
@Order(AopOrder.LAYERED_CACHEABLE)
@Component
class LayeredCacheableAspect(
    private val inMemoryCache: InMemoryCache,
    private val redisRepository: RedisRepository
) {
    private val log = LoggerFactory.getLogger(LayeredCacheableAspect::class.java)

    @Around("@annotation(layeredCacheable)")
    fun layeredCache(joinPoint: ProceedingJoinPoint, layeredCacheable: LayeredCacheable): Any? {
        val methodSignature = joinPoint.signature as MethodSignature
        val method = methodSignature.method
        val args = joinPoint.args
        val parameterNames = methodSignature.parameterNames

        // 반환 타입에서 List<T>의 T 추출
        val genericReturnType = method.genericReturnType
        val elementType = if (genericReturnType is ParameterizedType) {
            genericReturnType.actualTypeArguments.firstOrNull() as? Class<*>
        } else null

        // since 인자 값이 있는 경우, 캐시 키에 '3d' 같은 형식으로 추가
        val indexOfSince = parameterNames.indexOf("since")
        val since = if (indexOfSince != -1 && args[indexOfSince] is LocalDate) {
            args[indexOfSince] as LocalDate
        } else null

        // 캐시 키 생성
        val cacheKey = CacheKeyGenerator.generate(
            layeredCacheable.resourceName,
            layeredCacheable.displayName,
            since
        )

        if (elementType == null) {
            log.warn("⚠️ 캐시에 활용할 List 요소 타입을 추론할 수 없습니다.")
            return joinPoint.proceed()
        }

        // In-Memory 캐시 조회
        val inMemoryCacheEntries = inMemoryCache.getList(cacheKey, elementType)
        if (inMemoryCacheEntries.isNotEmpty()) {
            log.info("✅ [In-Memory Cache Hit]")
            return inMemoryCacheEntries
        }

        // Redis 캐시 조회
        val redisCacheEntries = redisRepository.findList(cacheKey, elementType)
        if (redisCacheEntries.isNotEmpty()) {
            log.info("✅ [Redis Cache Hit] → In-Memory Cache Put")
            inMemoryCache.put(cacheKey, redisCacheEntries)
            return redisCacheEntries
        }

        // Proceed
        log.info("⚠️ [Cache Miss] → 기존 로직 수행")
        val result = joinPoint.proceed()

        if (result is List<*>) {
            inMemoryCache.put(cacheKey, result)
            redisRepository.save(cacheKey, result, Duration.ofSeconds(layeredCacheable.redisTtlSeconds))
            log.info("💾 [Cache Save] Key: $cacheKey")
        }

        return result
    }
}

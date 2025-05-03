package kr.hhplus.be.server.domain.product

import kr.hhplus.be.server.infrastructure.redis.RedisRepository
import kr.hhplus.be.server.support.cache.InMemoryCache
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDate

@Service
class StatisticService(
    private val statisticRepository: StatisticRepository,
    private val inMemoryCache: InMemoryCache,
    private val redisRepository: RedisRepository
) {
    companion object {
        private const val CACHE_KEY = "product:popular:3d"
        private val TTL = Duration.ofHours(13L)
    }

    private val log = LoggerFactory.getLogger(StatisticService::class.java)

    fun record(statistic: Statistic): Statistic {
        return statisticRepository.save(statistic)
    }

    fun getTop5PopularProductStatistics(since: LocalDate): List<ProductInfo.Popular> {
        val isThreeDayRequest = since == LocalDate.now().minusDays(3)
        if (isThreeDayRequest) {
            log.debug("⏳ [Cache Check] 3일 기준 인기 상품 조회")

            inMemoryCache.getList(CACHE_KEY, PopularProductCacheEntry::class.java)
                .takeIf { it.isNotEmpty() }
                ?.let {
                    log.info("✅ [In-Memory Cache Hit]")
                    return it.map { cache -> cache.toInfo() }
                }

            redisRepository.findList(CACHE_KEY, PopularProductCacheEntry::class.java)
                .takeIf { it.isNotEmpty() }
                ?.let {
                    log.info("✅ [Redis Cache Hit] → In-Memory Cache Put")
                    inMemoryCache.put(CACHE_KEY, it)
                    return it.map { cache -> cache.toInfo() }
                }

            log.info("⚠️ [Cache Miss] → DB 조회")
        }

        val startOfDay = since.atStartOfDay()
        val records = statisticRepository.findTop5ProductSales(startOfDay)
        val popularProducts = records.map { ProductInfo.Popular.from(it) }

        // In-Memory 및 Redis 모두 캐시 미스 발생 시 둘 다 Cache Put
        if (isThreeDayRequest) {
            val cacheEntries = popularProducts.map { PopularProductCacheEntry.from(it) }
            redisRepository.save(CACHE_KEY, cacheEntries, Duration.ofHours(13))
            inMemoryCache.put(CACHE_KEY, cacheEntries)
            log.info("💾 [Cache Save] 인기 상품 캐시 저장")
        }

        return popularProducts
    }

    fun cachePopularProducts(since: LocalDate) {
        val popularProducts = getTop5PopularProductStatistics(since)
        val cachedPopularProducts = popularProducts.map { PopularProductCacheEntry.from(it) }
        redisRepository.save(CACHE_KEY, cachedPopularProducts, TTL)
    }
}

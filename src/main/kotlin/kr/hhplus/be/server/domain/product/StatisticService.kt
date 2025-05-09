package kr.hhplus.be.server.domain.product

import kr.hhplus.be.server.infrastructure.redis.RedisRepository
import kr.hhplus.be.server.support.aop.LayeredCacheable
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDate

@Service
class StatisticService(
    private val statisticRepository: StatisticRepository,
    private val redisRepository: RedisRepository
) {
    companion object {
        private const val CACHE_KEY = "product:popular:3d"
        private val TTL = Duration.ofHours(13L)
    }

    fun record(statistic: Statistic): Statistic {
        return statisticRepository.save(statistic)
    }

    @LayeredCacheable(resourceName = "product", displayName = "popular")
    fun getTop5PopularProductStatistics(since: LocalDate): List<ProductInfo.Popular> {
        val startOfDay = since.atStartOfDay()
        val records = statisticRepository.findTop5ProductSales(startOfDay)
        val popularProducts = records.map { ProductInfo.Popular.from(it) }

        return popularProducts
    }

    fun cachePopularProducts(since: LocalDate) {
        val popularProducts = getTop5PopularProductStatistics(since)
        val cachedPopularProducts = popularProducts.map { PopularProductCacheEntry.from(it) }
        redisRepository.save(CACHE_KEY, cachedPopularProducts, TTL)
    }
}

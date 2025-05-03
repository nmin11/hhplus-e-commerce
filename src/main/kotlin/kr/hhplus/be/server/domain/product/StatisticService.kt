package kr.hhplus.be.server.domain.product

import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class StatisticService(
    private val statisticRepository: StatisticRepository
) {
    fun record(statistic: Statistic): Statistic {
        return statisticRepository.save(statistic)
    }

    fun getTop5PopularProductStatistics(since: LocalDate): List<ProductInfo.Popular> {
        val startOfDay = since.atStartOfDay()
        val records = statisticRepository.findTop5ProductSales(startOfDay)
        return records.map { ProductInfo.Popular.from(it) }
    }

    fun cachePopularProducts(since: LocalDate) {
        val popularProducts = getTop5PopularProductStatistics(since)
        val cachedPopularProducts = popularProducts.map { PopularProductCacheEntry.from(it) }
        redisRepository.save(CACHE_KEY, cachedPopularProducts, TTL)
    }
}

package kr.hhplus.be.server.domain.product

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.server.infrastructure.product.PopularProductRecord
import kr.hhplus.be.server.infrastructure.redis.RedisRepository
import kr.hhplus.be.server.support.cache.InMemoryCache
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.LocalDate

class StatisticServiceTest {
    private val statisticRepository = mockk<StatisticRepository>()
    private val inMemoryCache = mockk<InMemoryCache>()
    private val redisRepository = mockk<RedisRepository>()
    private val statisticService = StatisticService(statisticRepository, redisRepository)

    @Test
    @DisplayName("통계 정보를 저장하고 반환")
    fun record_shouldSaveAndReturnStatistic() {
        // given
        val product = Product.create(name = "청바지", basePrice = 39000)
        val statistic = Statistic.create(product = product, salesCount = 10)

        every { statisticRepository.save(statistic) } returns statistic

        // when
        val result = statisticService.record(statistic)

        // then
        assertThat(result).isEqualTo(statistic)
        verify(exactly = 1) { statisticRepository.save(statistic) }
    }

    @Test
    @DisplayName("지정된 날짜 이후의 판매량 상위 5개 상품 통계 반환")
    fun returnTop5StatisticsSinceDate() {
        // given
        val since = LocalDate.now().minusDays(3)
        val startOfDay = since.atStartOfDay()

        val stat1 = PopularProductRecord(
            id = 2001L,
            name = "청바지",
            basePrice = 10_000,
            totalSales = 12
        )
        val stat2 = PopularProductRecord(
            id = 2002L,
            name = "셔츠",
            basePrice = 12_000,
            totalSales = 9
        )
        val records = listOf(stat1, stat2)
        val infos = records.map { ProductInfo.Popular.from(it) }
        val cached = infos.map { PopularProductCacheEntry.from(it) }

        every {
            statisticRepository.findTop5ProductSales(startOfDay)
        } returns records
        every {
            inMemoryCache.getList("product:popular:3d", PopularProductCacheEntry::class.java)
        } returns cached
        every {
            redisRepository.findList("product:popular:3d", PopularProductCacheEntry::class.java)
        } returns cached

        // when
        val result = statisticService.getTop5PopularProductStatistics(since)

        // then
        assertThat(result).isEqualTo(result)
    }

    @Test
    @DisplayName("인기 상품 목록을 조회하여 캐시에 저장")
    fun cachePopularProducts_shouldSaveListToRedis() {
        // given
        val since = LocalDate.now().minusDays(3)
        val startOfDay = since.atStartOfDay()

        val stat1 = PopularProductRecord(
            id = 2001L,
            name = "청바지",
            basePrice = 10_000,
            totalSales = 12
        )
        val stat2 = PopularProductRecord(
            id = 2002L,
            name = "셔츠",
            basePrice = 12_000,
            totalSales = 9
        )

        val records = listOf(stat1, stat2)
        val infos = records.map { ProductInfo.Popular.from(it) }
        val cached = infos.map { PopularProductCacheEntry.from(it) }

        every {
            statisticRepository.findTop5ProductSales(startOfDay)
        } returns records

        every {
            redisRepository.save("product:popular:3d", cached, Duration.ofHours(13))
        } just Runs

        // when
        statisticService.cachePopularProducts(since)

        // then
        verify(exactly = 1) {
            statisticRepository.findTop5ProductSales(startOfDay)
            redisRepository.save("product:popular:3d", cached, Duration.ofHours(13))
        }
    }
}

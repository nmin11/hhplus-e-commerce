package kr.hhplus.be.server.domain.product

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.server.infrastructure.product.PopularProductRecord
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDate

class StatisticServiceTest {
    private val statisticRepository = mockk<StatisticRepository>()
    private val statisticService = StatisticService(statisticRepository)

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

        every {
            statisticRepository.findTop5ProductSales(startOfDay)
        } returns records

        // when
        val result = statisticService.getTop5PopularProductStatistics(since)

        // then
        assertThat(result).isEqualTo(result)
    }
}

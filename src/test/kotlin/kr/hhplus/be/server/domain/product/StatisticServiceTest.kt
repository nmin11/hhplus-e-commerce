package kr.hhplus.be.server.domain.product

import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime

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
        val product = Product.create(name = "청바지", basePrice = 10_000)
        val stat1 = spyk(Statistic.create(product = product, salesCount = 12))
        val stat2 = spyk(Statistic.create(product = product, salesCount = 9))
        val statistics = listOf(stat1, stat2)

        every { stat1.soldAt } returns LocalDateTime.now().minusDays(1)
        every { stat2.soldAt } returns LocalDateTime.now().minusDays(2)
        every { statisticRepository.findTop5BySoldAtAfterOrderBySalesCountDesc(startOfDay) } returns statistics

        // when
        val result = statisticService.getTop5PopularProductStatistics(since)

        // then
        assertThat(result).isEqualTo(statistics)
    }
}

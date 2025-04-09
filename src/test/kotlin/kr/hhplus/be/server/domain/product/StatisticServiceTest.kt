package kr.hhplus.be.server.domain.product

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime

class StatisticServiceTest {
    private val statisticRepository = mockk<StatisticRepository>()
    private val statisticService = StatisticService(statisticRepository)

    @Test
    @DisplayName("지정된 날짜 이후의 판매량 상위 5개 상품 통계 반환")
    fun returnTop5StatisticsSinceDate() {
        // given
        val since = LocalDate.now().minusDays(3)
        val product = Product(name = "청바지", basePrice = 10_000).apply { id = 1L }
        val statistics = listOf(
            Statistic(product, salesCount = 12).apply {
                id = 1L
                soldAt = LocalDateTime.now().minusDays(1)
            },
            Statistic(product, salesCount = 9).apply {
                id = 2L
                soldAt = LocalDateTime.now().minusDays(2)
            }
        )
        every { statisticRepository.findTop5BySoldAtAfterOrderBySalesCountDesc(since) } returns statistics

        // when
        val result = statisticService.getTop5PopularProductStatistics(since)

        // then
        assertThat(result).isEqualTo(statistics)
    }
}

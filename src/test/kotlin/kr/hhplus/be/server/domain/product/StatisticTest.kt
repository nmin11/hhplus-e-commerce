package kr.hhplus.be.server.domain.product

import kr.hhplus.be.server.support.exception.product.StatisticInvalidSalesCountException
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName

class StatisticTest {
    private val product = Product.create("청바지", 30000)

    @Test
    @DisplayName("유효한 판매 수량으로 Statistic 생성 성공")
    fun create_shouldReturnStatistic_whenValidSalesCount() {
        // when
        val stat = Statistic.create(product, 10)

        // then
        assertThat(stat.product).isEqualTo(product)
        assertThat(stat.salesCount).isEqualTo(10)
        assertThat(stat.soldAt).isNotNull()
    }

    @Test
    @DisplayName("판매 수량이 0 이하일 경우 예외 발생")
    fun create_throwException_whenSalesCountIsInvalid() {
        val exception = assertThrows(StatisticInvalidSalesCountException::class.java) {
            Statistic.create(product, 0)
        }

        assertThat(exception.message).isEqualTo("판매 수량은 0보다 커야 합니다.")
    }
}

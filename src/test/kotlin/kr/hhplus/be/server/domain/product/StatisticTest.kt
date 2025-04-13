package kr.hhplus.be.server.domain.product

import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName

class StatisticTest {
    private val product = Product.create("청바지", 30000).apply { id = 1L }

    @Nested
    inner class Create {
        @Test
        @DisplayName("유효한 판매 수량으로 Statistic 생성 성공")
        fun shouldReturnStatistic_whenValidSalesCount() {
            // when
            val stat = Statistic.create(product, 10)

            // then
            assertThat(stat.product).isEqualTo(product)
            assertThat(stat.salesCount).isEqualTo(10)
            assertThat(stat.soldAt).isNotNull()
        }

        @Test
        @DisplayName("판매 수량이 0 이하일 경우 예외 발생")
        fun throwException_whenSalesCountIsInvalid() {
            val exception = assertThrows(IllegalArgumentException::class.java) {
                Statistic.create(product, 0)
            }

            assertThat(exception.message).isEqualTo("판매 수량은 0보다 커야 합니다.")
        }
    }

    @Nested
    inner class RequireSavedId {
        @Test
        @DisplayName("ID가 존재하면 해당 ID 반환")
        fun shouldReturnId_whenExists() {
            // given
            val stat = Statistic.create(product, 5).apply { id = 100L }

            // when
            val result = stat.requireSavedId()

            // then
            assertThat(result).isEqualTo(100L)
        }

        @Test
        @DisplayName("ID가 null 이면 예외 발생")
        fun shouldThrowException_whenIdIsNull() {
            // given
            val stat = Statistic.create(product, 5)

            // when
            val exception = assertThrows(IllegalStateException::class.java) {
                stat.requireSavedId()
            }

            // then
            assertThat(exception.message).isEqualTo("Statistic 객체가 저장되지 않았습니다.")
        }
    }
}

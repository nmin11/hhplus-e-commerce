package kr.hhplus.be.server.application.product

import kr.hhplus.be.server.support.exception.product.StatisticInvalidPeriodConditionException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import java.time.LocalDate

class ProductCriteriaTest {
    @Nested
    inner class PeriodConditionTest {
        @Test
        @DisplayName("하나의 조건만 주어졌을 때 정상적으로 생성")
        fun init_shouldSucceed_whenOnlyOneConditionProvided() {
            // when & then
            assertDoesNotThrow { ProductCriteria.PeriodCondition(days = 3) }
            assertDoesNotThrow { ProductCriteria.PeriodCondition(weeks = 1) }
            assertDoesNotThrow { ProductCriteria.PeriodCondition(months = 2) }
        }

        @Test
        @DisplayName("두 개 이상의 조건이 주어졌을 때 예외 발생")
        fun init_shouldFail_whenMultipleConditionsProvided() {
            // when & then
            assertThrows<StatisticInvalidPeriodConditionException> {
                ProductCriteria.PeriodCondition(days = 3, weeks = 1)
            }
            assertThrows<StatisticInvalidPeriodConditionException> {
                ProductCriteria.PeriodCondition(days = 3, months = 1)
            }
            assertThrows<StatisticInvalidPeriodConditionException> {
                ProductCriteria.PeriodCondition(days = 3, weeks = 1, months = 2)
            }
        }

        @Test
        @DisplayName("toStartDate 는 해당 조건에 맞게 날짜를 계산함")
        fun toStartDate_shouldReturnCorrectDate() {
            // given
            val today = LocalDate.now()

            // when & then
            assertThat(ProductCriteria.PeriodCondition(days = 5).toStartDate())
                .isEqualTo(today.minusDays(5))
            assertThat(ProductCriteria.PeriodCondition(weeks = 2).toStartDate())
                .isEqualTo(today.minusWeeks(2))
            assertThat(ProductCriteria.PeriodCondition(months = 1).toStartDate())
                .isEqualTo(today.minusMonths(1))
        }
    }
}

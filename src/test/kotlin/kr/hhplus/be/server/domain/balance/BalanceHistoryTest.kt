package kr.hhplus.be.server.domain.balance

import kr.hhplus.be.server.domain.customer.Customer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class BalanceHistoryTest {
    private val customer = Customer.create("tester").apply { id = 1L }

    @Nested
    inner class RequireSavedId {
        @Test
        @DisplayName("ID가 존재하면 해당 ID를 반환")
        fun shouldReturnId_whenPresent() {
            val history = BalanceHistory.charge(customer, 1000, 5000).apply { id = 42L }

            val result = history.requireSavedId()

            assertThat(result).isEqualTo(42L)
        }

        @Test
        @DisplayName("ID가 null 이면 예외 발생")
        fun shouldThrow_whenIdIsNull() {
            val history = BalanceHistory.charge(customer, 1000, 5000)

            val exception = assertThrows<IllegalStateException> {
                history.requireSavedId()
            }

            assertThat(exception.message).isEqualTo("BalanceHistory 객체가 저장되지 않았습니다.")
        }
    }

    @Nested
    inner class FactoryMethods {
        @Test
        @DisplayName("충전 이력 생성 시 타입은 CHARGE")
        fun shouldCreateChargeHistory() {
            val history = BalanceHistory.charge(customer, 1000, 5000)

            assertThat(history.changeType).isEqualTo(BalanceChangeType.CHARGE)
            assertThat(history.changeAmount).isEqualTo(1000)
            assertThat(history.totalAmount).isEqualTo(5000)
        }

        @Test
        @DisplayName("사용 이력 생성 시 타입은 USE")
        fun shouldCreateUseHistory() {
            val history = BalanceHistory.use(customer, 3000, 2000)

            assertThat(history.changeType).isEqualTo(BalanceChangeType.USE)
            assertThat(history.changeAmount).isEqualTo(3000)
            assertThat(history.totalAmount).isEqualTo(2000)
        }
    }
}

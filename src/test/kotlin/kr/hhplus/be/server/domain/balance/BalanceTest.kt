package kr.hhplus.be.server.domain.balance

import kr.hhplus.be.server.domain.customer.Customer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class BalanceTest {
    private val customer = Customer.create("tester")

    @Nested
    inner class Charge {
        @Test
        @DisplayName("금액을 충전하면 잔액이 증가한다")
        fun increaseAmount_whenValidAmountGiven() {
            // given
            val balance = Balance.create(customer, 10_000)

            // when
            balance.charge(5_000)

            // then
            assertThat(balance.getAmount()).isEqualTo(15_000)
        }

        @Test
        @DisplayName("충전 금액이 0 이하이면 예외 발생")
        fun throwException_whenAmountIsZeroOrNegative() {
            val balance = Balance.create(customer, 10_000)
            val invalidAmounts = listOf(0, -1_000)

            for (amount in invalidAmounts) {
                val exception = assertThrows<IllegalArgumentException> {
                    balance.charge(amount)
                }

                assertThat(exception.message).isEqualTo("충전 금액은 0보다 커야 합니다.")
            }
        }
    }

    @Nested
    inner class Deduct {
        @Test
        @DisplayName("잔액에서 금액 차감")
        fun decreaseAmount_whenSufficientBalance() {
            // given
            val balance = Balance.create(customer, 20_000)

            // when
            balance.deduct(5_000)

            // then
            assertThat(balance.getAmount()).isEqualTo(15_000)
        }

        @Test
        @DisplayName("차감 금액이 0 이하일 경우 예외 발생")
        fun throwException_whenAmountIsZeroOrNegative() {
            val balance = Balance.create(customer, 10_000)
            val invalidAmounts = listOf(0, -5_000)

            for (amount in invalidAmounts) {
                val exception = assertThrows<IllegalArgumentException> {
                    balance.deduct(amount)
                }

                assertThat(exception.message).isEqualTo("차감 금액은 0보다 커야 합니다.")
            }
        }

        @Test
        @DisplayName("잔액보다 큰 금액을 차감할 경우 예외 발생")
        fun throwException_whenInsufficientBalance() {
            val balance = Balance.create(customer, 5_000)

            val exception = assertThrows<IllegalStateException> {
                balance.deduct(10_000)
            }

            assertThat(exception.message).isEqualTo("잔액이 부족합니다.")
        }
    }
}

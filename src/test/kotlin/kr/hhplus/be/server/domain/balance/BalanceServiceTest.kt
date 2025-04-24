package kr.hhplus.be.server.domain.balance

import io.mockk.every
import io.mockk.mockk
import kr.hhplus.be.server.domain.customer.Customer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*

class BalanceServiceTest {
    private val balanceRepository = mockk<BalanceRepository>()
    private val balanceService = BalanceService(balanceRepository)

    @Nested
    inner class GetByCustomerId {
        private val customerId = 1L

        @Test
        @DisplayName("잔액 정보가 존재할 경우 잔액 반환")
        fun whenExists_thenReturnBalance() {
            // given
            val customer = Customer.create(username = "tester")
            val expectedBalance = Balance.create(customer, amount = 100_000)
            every { balanceRepository.findByCustomerId(customerId) } returns expectedBalance

            // when
            val result = balanceService.getByCustomerId(customerId)

            // then
            assertThat(result).isEqualTo(expectedBalance)
        }

        @Test
        @DisplayName("잔액 정보가 존재하지 않으면 예외 발생")
        fun whenNonExistence_thenThrowException() {
            // given
            every { balanceRepository.findByCustomerId(customerId) } returns null

            // when
            val exception = assertThrows<IllegalStateException> {
                balanceService.getByCustomerId(customerId)
            }

            // then
            assertThat(exception)
                .isInstanceOf(IllegalStateException::class.java)
                .hasMessage("잔액 정보가 존재하지 않습니다.")
        }
    }

    @Nested
    inner class Charge {
        @Test
        @DisplayName("충전 시 기존 잔액에 금액이 더해짐")
        fun shouldAddAmountToBalance() {
            // given
            val customer = Customer.create(username = "tester")
            val balance = Balance.create(customer, amount = 100_000)
            every { balanceRepository.findByCustomerId(1L) } returns balance
            every { balanceRepository.saveAndFlush(any()) } answers { firstArg() }

            // when
            val result = balanceService.charge(1L, 50_000)

            // then
            assertThat(result.getAmount()).isEqualTo(150_000)
        }

        @Test
        @DisplayName("충전 금액이 0 이하일 경우 예외 발생")
        fun shouldThrow_whenAmountInvalid() {
            val customer = Customer.create(username = "tester")
            val balance = Balance.create(customer, amount = 100_000)
            every { balanceRepository.findByCustomerId(1L) } returns balance
            val invalidAmounts = listOf(0, -100)

            for (amount in invalidAmounts) {
                // when
                val exception = assertThrows<IllegalArgumentException> {
                    balanceService.charge(1L, amount)
                }

                // then
                assertThat(exception)
                    .isInstanceOf(IllegalArgumentException::class.java)
                    .hasMessage("충전 금액은 0보다 커야 합니다.")
            }
        }

        @Test
        @DisplayName("존재하지 않는 잔액 정보로 충전 시 예외 발생")
        fun shouldThrow_whenBalanceNotFound() {
            every { balanceRepository.findByCustomerId(1L) } returns null

            val exception = assertThrows<IllegalStateException> {
                balanceService.charge(1L, 10_000)
            }

            assertThat(exception)
                .isInstanceOf(IllegalStateException::class.java)
                .hasMessage("잔액 정보가 존재하지 않습니다.")
        }
    }

    @Nested
    inner class Deduct {
        @Test
        @DisplayName("차감 금액이 잔액보다 적거나 같으면 차감 성공")
        fun shouldSubtractAmount_whenSufficient() {
            val customer = Customer.create(username = "tester")
            val balance = Balance.create(customer, amount = 100_000)
            every { balanceRepository.findByCustomerId(1L) } returns balance
            every { balanceRepository.saveAndFlush(any()) } answers { firstArg() }

            val result = balanceService.deduct(1L, 30_000)

            assertThat(result.getAmount()).isEqualTo(70_000)
        }

        @Test
        @DisplayName("차감 금액이 0 이하일 경우 예외 발생")
        fun shouldThrow_whenAmountInvalid() {
            val customer = Customer.create(username = "tester")
            val balance = Balance.create(customer, amount = 100_000)
            every { balanceRepository.findByCustomerId(1L) } returns balance
            val invalidAmounts = listOf(0, -10_000)

            for (amount in invalidAmounts) {
                val exception = assertThrows<IllegalArgumentException> {
                    balanceService.deduct(1L, amount)
                }

                assertThat(exception)
                    .isInstanceOf(IllegalArgumentException::class.java)
                    .hasMessage("차감 금액은 0보다 커야 합니다.")
            }
        }

        @Test
        @DisplayName("잔액 정보가 존재하지 않으면 예외 발생")
        fun shouldThrow_whenBalanceNotFound() {
            every { balanceRepository.findByCustomerId(1L) } returns null

            val exception = assertThrows<IllegalStateException> {
                balanceService.deduct(1L, 10_000)
            }

            assertThat(exception)
                .isInstanceOf(IllegalStateException::class.java)
                .hasMessage("잔액 정보가 존재하지 않습니다.")
        }

        @Test
        @DisplayName("잔액이 부족할 경우 예외 발생")
        fun shouldThrow_whenInsufficientBalance() {
            val customer = Customer.create(username = "tester")
            val balance = Balance.create(customer, amount = 20_000)
            every { balanceRepository.findByCustomerId(1L) } returns balance

            val exception = assertThrows<IllegalStateException> {
                balanceService.deduct(1L, 30_000)
            }

            assertThat(exception)
                .isInstanceOf(IllegalStateException::class.java)
                .hasMessage("잔액이 부족합니다.")
        }
    }
}

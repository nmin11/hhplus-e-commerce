package kr.hhplus.be.server.interfaces.balance

import io.mockk.every
import io.mockk.mockk
import kr.hhplus.be.server.application.balance.BalanceCommand
import kr.hhplus.be.server.application.balance.BalanceFacade
import kr.hhplus.be.server.application.balance.BalanceResult
import kr.hhplus.be.server.domain.balance.Balance
import kr.hhplus.be.server.domain.balance.BalanceChangeType
import kr.hhplus.be.server.domain.balance.BalanceHistory
import kr.hhplus.be.server.domain.customer.Customer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class BalanceControllerTest {
    private val balanceFacade = mockk<BalanceFacade>()
    private val balanceController = BalanceController(balanceFacade)

    @Test
    @DisplayName("잔액 조회 요청 시 잔액 정보 반환")
    fun getBalance_shouldReturnBalanceSummary() {
        // given
        val customerId = 1L
        val customer = Customer(username = "tester").apply { id = customerId }
        val balance = Balance(customer, amount = 100_000).apply { id = 1L }
        val result = BalanceResult.Summary.from(balance)
        every { balanceFacade.getBalance(customerId) } returns result

        // when
        val response = balanceController.getBalance(customerId)

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isEqualTo(BalanceResponse.Summary.from(result))
    }

    @Test
    @DisplayName("잔액 변경 이력 조회 시 히스토리 리스트 반환")
    fun getBalanceHistories_shouldReturnHistoryList() {
        // given
        val customerId = 1L
        val histories = listOf(
            BalanceHistory(
                customer = Customer("tester").apply { id = customerId },
                changeType = BalanceChangeType.CHARGE,
                changeAmount = 10_000,
                totalAmount = 110_000
            ).apply { id = 1L }
        )
        val results = histories.map { BalanceResult.History.from(it) }
        every { balanceFacade.getHistories(customerId) } returns results

        // when
        val response = balanceController.getBalanceHistories(customerId)

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isEqualTo(results.map { BalanceResponse.History.from(it) })
    }

    @Test
    @DisplayName("잔액 충전 요청 시 갱신된 잔액 반환")
    fun chargeBalance_shouldReturnUpdatedBalance() {
        // given
        val customerId = 1L
        val customer = Customer(username = "tester").apply { id = customerId }
        val request = BalanceRequest.Charge(customerId, amount = 50_000)
        val updatedBalance = Balance(customer, amount = 150_000).apply { id = 1L }
        val result = BalanceResult.Summary.from(updatedBalance)
        every { balanceFacade.charge(BalanceCommand.Charge.from(request)) } returns result

        // when
        val response = balanceController.chargeBalance(request)

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isEqualTo(BalanceResponse.Summary.from(result))
    }
}

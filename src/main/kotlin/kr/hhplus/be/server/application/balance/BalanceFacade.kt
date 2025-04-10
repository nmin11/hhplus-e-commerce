package kr.hhplus.be.server.application.balance

import kr.hhplus.be.server.domain.balance.*
import kr.hhplus.be.server.domain.customer.CustomerService
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class BalanceFacade(
    private val balanceService: BalanceService,
    private val balanceHistoryService: BalanceHistoryService,
    private val customerService: CustomerService,
) {
    fun getBalance(customerId: Long): BalanceResult.Summary {
        customerService.validateCustomerExistence(customerId)
        val balance = balanceService.getByCustomerId(customerId)
        return BalanceResult.Summary.from(balance)
    }

    fun getHistories(customerId: Long): List<BalanceResult.History> {
        customerService.validateCustomerExistence(customerId)
        val histories = balanceHistoryService.getAllByCustomerId(customerId)
        return histories.map { BalanceResult.History.from(it) }
    }

    @Transactional
    fun charge(command: BalanceCommand.Charge): BalanceResult.Summary {
        // 1. 고객 조회
        val customer = customerService.getById(command.customerId)

        // 2. 잔액 충전
        val updatedBalance = balanceService.charge(command.customerId, command.amount)

        // 3. 충전 내역 저장
        balanceHistoryService.create(
            BalanceHistory(
                customer,
                changeType = BalanceChangeType.CHARGE,
                changeAmount = command.amount,
                totalAmount = updatedBalance.amount
            )
        )

        return BalanceResult.Summary.from(updatedBalance)
    }
}

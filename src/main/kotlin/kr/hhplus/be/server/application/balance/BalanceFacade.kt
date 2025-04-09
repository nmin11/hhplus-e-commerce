package kr.hhplus.be.server.application.balance

import kr.hhplus.be.server.domain.balance.*
import kr.hhplus.be.server.domain.customer.CustomerService
import org.springframework.stereotype.Component

@Component
class BalanceFacade(
    private val balanceService: BalanceService,
    private val balanceHistoryService: BalanceHistoryService,
    private val customerService: CustomerService,
) {
    fun getBalance(customerId: Long): Balance {
        customerService.validateCustomerExistence(customerId)
        return balanceService.getByCustomerId(customerId)
    }

    fun getHistories(customerId: Long): List<BalanceHistory> {
        customerService.validateCustomerExistence(customerId)
        return balanceHistoryService.getAllByCustomerId(customerId)
    }

    fun charge(customerId: Long, amount: Int): Balance {
        val customer = customerService.getById(customerId)
        val updatedBalance = balanceService.charge(customerId, amount)
        balanceHistoryService.create(
            BalanceHistory(
                customer,
                changeType = BalanceChangeType.CHARGE,
                changeAmount = amount,
                totalAmount = updatedBalance.amount
            )
        )

        return updatedBalance
    }

    fun deduct(customerId: Long, amount: Int): Balance {
        // 1. 고객 존재 여부 확인
        customerService.validateCustomerExistence(customerId)

        // 2. 잔액 차감
        val balance = balanceService.deduct(customerId, amount)

        // 3. 차감 히스토리 기록
        val history = BalanceHistory(
            customer = balance.customer,
            changeType = BalanceChangeType.USE,
            changeAmount = amount,
            totalAmount = balance.amount
        )
        balanceHistoryService.create(history)

        return balance
    }
}

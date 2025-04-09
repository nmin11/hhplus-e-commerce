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
}

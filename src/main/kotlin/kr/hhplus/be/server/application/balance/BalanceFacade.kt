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
    fun getBalance(customerId: Long): Balance {
        customerService.validateCustomerExistence(customerId)
        return balanceService.getByCustomerId(customerId)
    }

    fun getHistories(customerId: Long): List<BalanceHistory> {
        customerService.validateCustomerExistence(customerId)
        return balanceHistoryService.getAllByCustomerId(customerId)
    }

    @Transactional
    fun charge(customerId: Long, amount: Int): Balance {
        // 1. 고객 조회
        val customer = customerService.getById(customerId)

        // 2. 잔액 충전
        val updatedBalance = balanceService.charge(customerId, amount)

        // 3. 충전 내역 저장
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

package kr.hhplus.be.server.application.balance

import kr.hhplus.be.server.domain.balance.Balance
import kr.hhplus.be.server.domain.balance.BalanceService
import kr.hhplus.be.server.domain.customer.CustomerService
import org.springframework.stereotype.Component

@Component
class BalanceFacade(
    private val customerService: CustomerService,
    private val balanceService: BalanceService
) {
    fun getByCustomerId(customerId: Long): Balance {
        customerService.validateCustomerExistence(customerId)
        return balanceService.getByCustomerId(customerId)
    }
}

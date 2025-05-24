package kr.hhplus.be.server.interfaces.balance

import kr.hhplus.be.server.domain.balance.event.BalanceDeductRequestedEvent
import kr.hhplus.be.server.domain.balance.event.BalanceDeductedEvent
import kr.hhplus.be.server.domain.balance.event.BalanceEventPublisher
import kr.hhplus.be.server.domain.balance.BalanceHistory
import kr.hhplus.be.server.domain.balance.BalanceHistoryService
import kr.hhplus.be.server.domain.balance.BalanceService
import kr.hhplus.be.server.domain.balance.event.BalanceRollbackRequestedEvent
import kr.hhplus.be.server.domain.customer.CustomerService
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class BalancePaymentEventListener(
    private val balanceService: BalanceService,
    private val balanceHistoryService: BalanceHistoryService,
    private val customerService: CustomerService,
    private val balanceEventPublisher: BalanceEventPublisher
) {
    @Transactional
    @EventListener
    fun handleDeduct(event: BalanceDeductRequestedEvent) {
        val customer = customerService.getById(event.customerId)
        val balance = balanceService.deduct(event.customerId, event.totalPrice)
        val history = BalanceHistory.use(
            customer,
            amount = event.totalPrice,
            updatedAmount = balance.getAmount()
        )

        balanceHistoryService.create(history)

        balanceEventPublisher.publish(BalanceDeductedEvent(event.orderId))
    }

    @Transactional
    @EventListener
    fun handleRollback(event: BalanceRollbackRequestedEvent) {
        val customer = customerService.getById(event.customerId)
        val balance = balanceService.charge(event.customerId, event.amount)
        val history = BalanceHistory.rollback(
            customer,
            amount = event.amount,
            updatedAmount = balance.getAmount()
        )

        balanceHistoryService.create(history)
    }
}

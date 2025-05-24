package kr.hhplus.be.server.application.payment

import kr.hhplus.be.server.application.payment.event.PaymentOrchestrator
import kr.hhplus.be.server.domain.order.OrderService
import org.springframework.stereotype.Component

@Component
class PaymentFacade(
    private val orderService: OrderService,
    private val paymentOrchestrator: PaymentOrchestrator
) {
    fun pay(command: PaymentCommand): PaymentResult {
        val order = orderService.getValidOrderForPayment(command.orderId)
        val customer = order.customer
        val customerId = customer.id

        paymentOrchestrator.startSage(order, customerId, command.couponId)

        return paymentOrchestrator.getResult()
    }
}

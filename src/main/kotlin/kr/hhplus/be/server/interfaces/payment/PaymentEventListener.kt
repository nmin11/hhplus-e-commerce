package kr.hhplus.be.server.interfaces.payment

import kr.hhplus.be.server.domain.coupon.CouponService
import kr.hhplus.be.server.domain.customer.CustomerService
import kr.hhplus.be.server.domain.order.OrderService
import kr.hhplus.be.server.domain.payment.Payment
import kr.hhplus.be.server.domain.payment.event.PaymentCreateRequestedEvent
import kr.hhplus.be.server.domain.payment.event.PaymentCreatedEvent
import kr.hhplus.be.server.domain.payment.event.PaymentEventPublisher
import kr.hhplus.be.server.domain.payment.PaymentService
import kr.hhplus.be.server.domain.payment.event.PaymentRollbackRequestedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class PaymentEventListener(
    private val couponService: CouponService,
    private val customerService: CustomerService,
    private val orderService: OrderService,
    private val paymentService: PaymentService,
    private val paymentEventPublisher: PaymentEventPublisher
) {
    @Transactional
    @EventListener
    fun handleCreate(event: PaymentCreateRequestedEvent) {
        val order = orderService.getById(event.orderId)
        val customer = customerService.getById(event.customerId)
        val coupon = event.couponId?.let { couponService.getById(event.couponId) }
        val payment = Payment.create(
            order = order,
            customer = customer,
            coupon = coupon,
            originalPrice = event.originalPrice,
            discountAmount = event.discountAmount,
        )

        paymentService.create(payment)
        orderService.markAsPaid(order)

        paymentEventPublisher.publish(PaymentCreatedEvent(payment))
    }

    @Transactional
    @EventListener
    fun handleRollback(event: PaymentRollbackRequestedEvent) {
        paymentService.deleteById(event.paymentId)
    }
}

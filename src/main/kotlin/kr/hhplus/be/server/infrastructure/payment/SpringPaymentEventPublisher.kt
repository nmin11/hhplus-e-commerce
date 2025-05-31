package kr.hhplus.be.server.infrastructure.payment

import kr.hhplus.be.server.domain.balance.event.BalanceDeductRequestedEvent
import kr.hhplus.be.server.domain.balance.event.BalanceRollbackRequestedEvent
import kr.hhplus.be.server.domain.coupon.event.CouponRollbackRequestedEvent
import kr.hhplus.be.server.domain.coupon.event.CouponUseRequestedEvent
import kr.hhplus.be.server.domain.payment.PaymentCompletedEvent
import kr.hhplus.be.server.domain.payment.event.PaymentCreateRequestedEvent
import kr.hhplus.be.server.domain.payment.event.PaymentCreatedEvent
import kr.hhplus.be.server.domain.payment.event.PaymentEventPublisher
import kr.hhplus.be.server.domain.payment.event.PaymentInitiatedEvent
import kr.hhplus.be.server.domain.payment.event.PaymentRollbackRequestedEvent
import kr.hhplus.be.server.domain.product.event.StatisticRecordRequestedEvent
import kr.hhplus.be.server.domain.product.event.StockDecreaseRequestedEvent
import kr.hhplus.be.server.domain.product.event.StockRollbackRequestedEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component

@Component
@Primary
class SpringPaymentEventPublisher(
    private val eventPublisher: ApplicationEventPublisher
) : PaymentEventPublisher {
    override fun publish(event: PaymentInitiatedEvent) {
        eventPublisher.publishEvent(event)
    }

    override fun publish(event: StockDecreaseRequestedEvent) {
        eventPublisher.publishEvent(event)
    }

    override fun publish(event: CouponUseRequestedEvent) {
        eventPublisher.publishEvent(event)
    }

    override fun publish(event: BalanceDeductRequestedEvent) {
        eventPublisher.publishEvent(event)
    }

    override fun publish(event: PaymentCreateRequestedEvent) {
        eventPublisher.publishEvent(event)
    }

    override fun publish(event: PaymentCreatedEvent) {
        eventPublisher.publishEvent(event)
    }

    override fun publish(event: StatisticRecordRequestedEvent) {
        eventPublisher.publishEvent(event)
    }

    override fun publish(event: PaymentCompletedEvent) {
        eventPublisher.publishEvent(event)
    }

    override fun publish(event: StockRollbackRequestedEvent) {
        eventPublisher.publishEvent(event)
    }

    override fun publish(event: CouponRollbackRequestedEvent) {
        eventPublisher.publishEvent(event)
    }

    override fun publish(event: BalanceRollbackRequestedEvent) {
        eventPublisher.publishEvent(event)
    }

    override fun publish(event: PaymentRollbackRequestedEvent) {
        eventPublisher.publishEvent(event)
    }
}

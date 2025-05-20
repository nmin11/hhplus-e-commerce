package kr.hhplus.be.server.infrastructure.payment

import kr.hhplus.be.server.domain.payment.PaymentCompletedEvent
import kr.hhplus.be.server.domain.payment.PaymentEventPublisher
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class PaymentEventPublisherImpl(
    private val eventPublisher: ApplicationEventPublisher
) : PaymentEventPublisher {
    override fun publish(event: PaymentCompletedEvent) {
        eventPublisher.publishEvent(event)
    }
}

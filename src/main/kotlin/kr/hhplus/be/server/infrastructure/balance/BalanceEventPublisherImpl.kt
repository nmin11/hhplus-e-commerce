package kr.hhplus.be.server.infrastructure.balance

import kr.hhplus.be.server.domain.balance.event.BalanceDeductedEvent
import kr.hhplus.be.server.domain.balance.event.BalanceEventPublisher
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class BalanceEventPublisherImpl(
    private val eventPublisher: ApplicationEventPublisher
) : BalanceEventPublisher {
    override fun publish(event: BalanceDeductedEvent) {
        eventPublisher.publishEvent(event)
    }
}

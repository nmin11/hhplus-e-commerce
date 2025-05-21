package kr.hhplus.be.server.infrastructure.product

import kr.hhplus.be.server.domain.product.event.StatisticEventPublisher
import kr.hhplus.be.server.domain.product.event.StatisticRecordedEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class StatisticEventPublisherImpl(
    private val eventPublisher: ApplicationEventPublisher
) : StatisticEventPublisher {
    override fun publish(event: StatisticRecordedEvent) {
        eventPublisher.publishEvent(event)
    }
}

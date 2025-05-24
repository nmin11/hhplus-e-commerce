package kr.hhplus.be.server.infrastructure.product

import kr.hhplus.be.server.domain.product.event.StockDecreasedEvent
import kr.hhplus.be.server.domain.product.event.StockEventPublisher
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class StockEventPublisherImpl(
    private val eventPublisher: ApplicationEventPublisher
) : StockEventPublisher {
    override fun publish(event: StockDecreasedEvent) {
        eventPublisher.publishEvent(event)
    }
}

package kr.hhplus.be.server.interfaces.product

import kr.hhplus.be.server.domain.product.event.StockDecreasedEvent
import kr.hhplus.be.server.domain.product.event.StockEventPublisher
import kr.hhplus.be.server.domain.product.StockService
import kr.hhplus.be.server.domain.product.event.StockDecreaseRequestedEvent
import kr.hhplus.be.server.domain.product.event.StockRollbackRequestedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class StockPaymentEventListener(
    private val stockService: StockService,
    private val stockEventPublisher: StockEventPublisher
) {
    @Transactional
    @EventListener
    fun handleStockDecrease(event: StockDecreaseRequestedEvent) {
        val items = event.items
        items.forEach {
            stockService.validate(it.productOptionId, it.quantity)
            stockService.decrease(it.productOptionId, it.quantity)
        }

        val stockDecreasedEvent = StockDecreasedEvent.from(event)
        stockEventPublisher.publish(stockDecreasedEvent)
    }

    @Transactional
    @EventListener
    fun handleRollback(event: StockRollbackRequestedEvent) {
        event.items.forEach {
            stockService.increase(it.productOptionId, it.quantity)
        }
    }
}

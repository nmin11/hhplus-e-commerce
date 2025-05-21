package kr.hhplus.be.server.domain.product.event

interface StockEventPublisher {
    fun publish(event: StockDecreasedEvent)
}

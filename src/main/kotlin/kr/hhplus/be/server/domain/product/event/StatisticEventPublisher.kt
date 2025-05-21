package kr.hhplus.be.server.domain.product.event

interface StatisticEventPublisher {
    fun publish(event: StatisticRecordedEvent)
}

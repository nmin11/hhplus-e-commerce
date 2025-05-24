package kr.hhplus.be.server.domain.balance.event

interface BalanceEventPublisher {
    fun publish(event: BalanceDeductedEvent)
}

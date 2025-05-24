package kr.hhplus.be.server.domain.balance.event

data class BalanceRollbackRequestedEvent(
    val customerId: Long,
    val amount: Int
)

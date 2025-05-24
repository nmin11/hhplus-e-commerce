package kr.hhplus.be.server.domain.balance.event

data class BalanceDeductRequestedEvent(
    val orderId: Long,
    val customerId: Long,
    val totalPrice: Int
)

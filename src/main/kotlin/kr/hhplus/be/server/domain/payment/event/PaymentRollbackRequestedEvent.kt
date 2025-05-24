package kr.hhplus.be.server.domain.payment.event

data class PaymentRollbackRequestedEvent(
    val paymentId: Long
)

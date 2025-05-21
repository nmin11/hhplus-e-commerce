package kr.hhplus.be.server.domain.payment.event

data class PaymentCreateRequestedEvent(
    val orderId: Long,
    val customerId: Long,
    val couponId: Long?,
    val originalPrice: Int,
    val discountAmount: Int
)

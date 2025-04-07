package kr.hhplus.be.server.interfaces.payment

data class PaymentRequest(
    val orderId: Long,
    val couponId: Long? = null
)

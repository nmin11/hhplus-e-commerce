package kr.hhplus.be.server.dto

data class PaymentRequest(
    val orderId: Long,
    val couponId: Long? = null
)

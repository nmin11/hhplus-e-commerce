package kr.hhplus.be.server.interfaces.dto.request

data class PaymentRequest(
    val orderId: Long,
    val couponId: Long? = null
)

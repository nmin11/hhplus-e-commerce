package kr.hhplus.be.server.interfaces.dto.response

data class PaymentResponse(
    val paymentId: Long,
    val orderId: Long,
    val customerId: Long,
    val originalPrice: Int,
    val discountAmount: Int,
    val discountedPrice: Int,
    val paidAt: String
)
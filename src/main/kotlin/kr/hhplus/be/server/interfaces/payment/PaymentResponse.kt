package kr.hhplus.be.server.interfaces.payment

import kr.hhplus.be.server.application.payment.PaymentResult

data class PaymentResponse(
    val paymentId: Long,
    val orderId: Long,
    val customerId: Long,
    val originalPrice: Int,
    val discountAmount: Int,
    val discountedPrice: Int,
    val paidAt: String
) {
    companion object {
        fun from(result: PaymentResult): PaymentResponse {
            return PaymentResponse(
                paymentId = result.paymentId,
                orderId = result.orderId,
                customerId = result.customerId,
                originalPrice = result.originalPrice,
                discountAmount = result.discountAmount,
                discountedPrice = result.discountedPrice,
                paidAt = result.paidAt
            )
        }
    }
}

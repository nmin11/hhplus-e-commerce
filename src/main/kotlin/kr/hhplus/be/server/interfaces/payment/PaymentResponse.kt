package kr.hhplus.be.server.interfaces.payment

import kr.hhplus.be.server.domain.payment.Payment

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
        fun from(payment: Payment): PaymentResponse {
            return PaymentResponse(
                paymentId = payment.requireSavedId(),
                orderId = payment.order.requireSavedId(),
                customerId = payment.customer.requireSavedId(),
                originalPrice = payment.originalPrice,
                discountAmount = payment.discountAmount,
                discountedPrice = payment.discountedPrice,
                paidAt = payment.paidAt.toString()
            )
        }
    }
}

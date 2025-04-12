package kr.hhplus.be.server.application.payment

import kr.hhplus.be.server.domain.payment.Payment

data class PaymentResult(
    val paymentId: Long,
    val orderId: Long,
    val customerId: Long,
    val originalPrice: Int,
    val discountAmount: Int,
    val discountedPrice: Int,
    val paidAt: String
) {
    companion object {
        fun from(payment: Payment): PaymentResult {
            return PaymentResult(
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

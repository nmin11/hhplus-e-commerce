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
                paymentId = payment.id ?: throw IllegalStateException("결제 ID가 없습니다."),
                orderId = payment.order.id ?: throw IllegalStateException("주문 ID가 없습니다."),
                customerId = payment.customer.id ?: throw IllegalStateException("고객 ID가 없습니다."),
                originalPrice = payment.originalPrice,
                discountAmount = payment.discountAmount,
                discountedPrice = payment.discountedPrice,
                paidAt = payment.paidAt.toString()
            )
        }
    }
}

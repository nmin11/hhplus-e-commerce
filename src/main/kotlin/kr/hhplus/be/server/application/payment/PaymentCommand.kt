package kr.hhplus.be.server.application.payment

import kr.hhplus.be.server.interfaces.payment.PaymentRequest

data class PaymentCommand(
    val orderId: Long,
    val couponId: Long?
) {
    companion object {
        fun from(request: PaymentRequest): PaymentCommand {
            return PaymentCommand(
                orderId = request.orderId,
                couponId = request.couponId
            )
        }
    }
}

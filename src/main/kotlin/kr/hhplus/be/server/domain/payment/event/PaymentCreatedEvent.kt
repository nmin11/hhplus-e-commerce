package kr.hhplus.be.server.domain.payment.event

import kr.hhplus.be.server.domain.payment.Payment

data class PaymentCreatedEvent(
    val payment: Payment
)

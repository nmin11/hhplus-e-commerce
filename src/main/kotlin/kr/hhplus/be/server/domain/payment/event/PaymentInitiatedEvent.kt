package kr.hhplus.be.server.domain.payment.event

import kr.hhplus.be.server.domain.order.Order
import kr.hhplus.be.server.domain.product.ProductInfo

data class PaymentInitiatedEvent(
    val order: Order,
    val customerId: Long,
    val couponId: Long?,
)

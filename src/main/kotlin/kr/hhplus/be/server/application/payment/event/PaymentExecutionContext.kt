package kr.hhplus.be.server.application.payment.event

import kr.hhplus.be.server.domain.order.Order
import kr.hhplus.be.server.domain.payment.Payment
import kr.hhplus.be.server.domain.product.ProductInfo

data class PaymentExecutionContext(
    val order: Order,
    val customerId: Long,
    val couponId: Long?,
    val totalPrice: Int,
    var payment: Payment? = null,
    var discountAmount: Int = 0,
    var stockDecreased: List<ProductInfo.StockItem>? = null,
    val completedSteps: MutableList<PaymentStep> = mutableListOf()
) {
    val hasCoupon: Boolean
        get() = couponId != null

    fun completeStep(step: PaymentStep) {
        completedSteps.add(step)
    }
}

package kr.hhplus.be.server.domain.order

import kr.hhplus.be.server.domain.customer.Customer
import kr.hhplus.be.server.domain.payment.Payment
import java.time.LocalDateTime

class Order(
    val customer: Customer,
    var totalPrice: Int,
) {
    var id: Long? = null
    var status = OrderStatus.CREATED
    val createdAt: LocalDateTime = LocalDateTime.now()
    var updatedAt: LocalDateTime = LocalDateTime.now()
    var payment: Payment? = null
    var orderItems: MutableList<OrderItem> = mutableListOf()
}

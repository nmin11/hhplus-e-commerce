package kr.hhplus.be.server.domain.order

import kr.hhplus.be.server.domain.customer.Customer
import kr.hhplus.be.server.domain.payment.Payment
import kr.hhplus.be.server.domain.product.ProductOption
import java.time.LocalDateTime

class Order private constructor(
    val customer: Customer
) {
    val id: Long = 0L
    var status = OrderStatus.CREATED
    val createdAt: LocalDateTime = LocalDateTime.now()
    var updatedAt: LocalDateTime = LocalDateTime.now()
    var totalPrice: Int = 0
    var payment: Payment? = null
    val orderItems: MutableList<OrderItem> = mutableListOf()

    companion object {
        fun create(customer: Customer): Order {
            return Order(customer)
        }

        fun createWithItems(customer: Customer, items: List<OrderItemInfo>): Order {
            val order = Order(customer)
            items.forEach { (option, quantity) ->
                order.addOrderItem(option, quantity)
            }
            return order
        }
    }

    fun addOrderItem(option: ProductOption, quantity: Int) {
        orderItems.add(OrderItem.create(this, option, quantity))
        totalPrice += (option.product.basePrice + option.extraPrice) * quantity
    }
}

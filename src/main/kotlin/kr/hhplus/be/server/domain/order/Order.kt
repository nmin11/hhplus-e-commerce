package kr.hhplus.be.server.domain.order

import jakarta.persistence.*
import kr.hhplus.be.server.domain.common.BaseEntity
import kr.hhplus.be.server.domain.customer.Customer
import kr.hhplus.be.server.domain.product.ProductOption

@Entity
@Table(name = "`order`")
class Order private constructor(
    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    val customer: Customer
) : BaseEntity() {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    var status = OrderStatus.CREATED

    @Column(name = "total_price", nullable = false)
    var totalPrice: Int = 0

    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], orphanRemoval = true)
    val orderItems: MutableList<OrderItem> = mutableListOf()

    companion object {
        fun create(customer: Customer): Order {
            return Order(customer)
        }

        fun createWithItems(customer: Customer, items: List<OrderItemInfo>): Order {
            if (items.isEmpty()) {
                throw IllegalArgumentException("주문 항목이 비어있습니다.")
            }

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

    fun markAsPaid() {
        if (this.status != OrderStatus.CREATED) {
            throw IllegalStateException("결제 가능한 상태가 아닙니다. (현재 상태: $status)")
        }

        this.status = OrderStatus.PAID
    }
}

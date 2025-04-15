package kr.hhplus.be.server.domain.order

import jakarta.persistence.*
import kr.hhplus.be.server.domain.product.ProductOption

@Entity
@Table(name = "order_item")
class OrderItem(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    val order: Order,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_option_id", nullable = false)
    val productOption: ProductOption,

    @Column(name = "quantity", nullable = false)
    val quantity: Int,

    @Column(name = "subtotal_price", nullable = false)
    val subtotalPrice: Int
) {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L

    companion object {
        fun create(order: Order, option: ProductOption, quantity: Int): OrderItem {
            val subtotal = (option.product.basePrice + option.extraPrice) * quantity
            return OrderItem(order, option, quantity, subtotal)
        }
    }
}

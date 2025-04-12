package kr.hhplus.be.server.domain.order

import kr.hhplus.be.server.domain.product.ProductOption

class OrderItem(
    val order: Order,
    val productOption: ProductOption,
    val quantity: Int,
    val subtotalPrice: Int
) {
    var id: Long? = null

    companion object {
        fun create(order: Order, option: ProductOption, quantity: Int): OrderItem {
            val subtotal = (option.product.basePrice + option.extraPrice) * quantity
            return OrderItem(order, option, quantity, subtotal)
        }
    }

    fun requireSavedId(): Long =
        id ?: throw IllegalStateException("OrderItem 객체가 저장되지 않았습니다.")
}

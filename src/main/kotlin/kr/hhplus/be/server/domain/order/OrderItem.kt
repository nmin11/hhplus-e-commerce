package kr.hhplus.be.server.domain.order

import kr.hhplus.be.server.domain.product.ProductOption

class OrderItem(
    val order: Order,
    val productOption: ProductOption,
    var quantity: Int,
    var subtotalPrice: Int
) {
    var id: Long? = null
}

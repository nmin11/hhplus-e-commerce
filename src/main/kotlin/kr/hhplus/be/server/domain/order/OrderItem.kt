package kr.hhplus.be.server.domain.order

class OrderItem(
    val orderId: Long,
    val productOptionId: Long,
    val quantity: Int,
    val subtotalPrice: Int
) {
    val id: Long? = null
}

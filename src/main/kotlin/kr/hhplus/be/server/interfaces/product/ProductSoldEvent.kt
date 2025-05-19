package kr.hhplus.be.server.interfaces.product

data class ProductSoldEvent(
    val items: List<SoldItem>
) {
    data class SoldItem(
        val productId: Long,
        val quantity: Int
    )
}

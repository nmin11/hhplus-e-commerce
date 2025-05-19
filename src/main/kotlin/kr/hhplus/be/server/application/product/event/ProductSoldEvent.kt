package kr.hhplus.be.server.application.product.event

data class ProductSoldEvent(
    val items: List<SoldItem>
) {
    data class SoldItem(
        val productId: Long,
        val quantity: Int
    )
}

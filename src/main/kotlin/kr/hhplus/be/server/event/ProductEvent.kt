package kr.hhplus.be.server.event

sealed class ProductEvent {
    data class SalesUpdated(
        val items: List<SoldItem>
    )

    data class SoldItem(
        val productId: Long,
        val quantity: Int
    )
}

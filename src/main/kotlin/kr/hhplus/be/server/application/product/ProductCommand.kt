package kr.hhplus.be.server.application.product

sealed class ProductCommand {
    data class SalesUpdated(
        val items: List<SoldItem>
    )

    data class SoldItem(
        val productId: Long,
        val quantity: Int
    )
}

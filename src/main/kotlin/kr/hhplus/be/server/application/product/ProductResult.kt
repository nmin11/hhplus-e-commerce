package kr.hhplus.be.server.application.product

sealed class ProductResult {
    data class Popular(
        val productId: Long,
        val name: String,
        val basePrice: Int,
        val salesCount: Int
    )
}

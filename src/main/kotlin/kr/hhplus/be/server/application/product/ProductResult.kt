package kr.hhplus.be.server.application.product

import kr.hhplus.be.server.domain.product.Product
import kr.hhplus.be.server.domain.product.ProductOption

sealed class ProductResult {
    data class Detail(
        val product: Product,
        val options: List<ProductOption>
    ) : ProductResult()

    data class Popular(
        val productId: Long,
        val name: String,
        val basePrice: Int,
        val salesCount: Int
    ) : ProductResult()
}

package kr.hhplus.be.server.application.product

import kr.hhplus.be.server.domain.product.Product
import kr.hhplus.be.server.domain.product.ProductOption
import kr.hhplus.be.server.domain.product.Statistic

sealed class ProductResult {
    data class Detail(
        val product: Product,
        val options: List<ProductOption>
    )

    data class Popular(
        val productId: Long,
        val name: String,
        val basePrice: Int,
        val salesCount: Int
    ) {
        companion object {
            fun from(statistic: Statistic): Popular {
                return Popular(
                    productId = statistic.product.id ?: throw IllegalStateException("상품 ID가 없습니다."),
                    name = statistic.product.name,
                    basePrice = statistic.product.basePrice,
                    salesCount = statistic.salesCount
                )
            }
        }
    }
}

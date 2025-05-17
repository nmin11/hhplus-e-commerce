package kr.hhplus.be.server.application.product

import kr.hhplus.be.server.domain.product.Product
import kr.hhplus.be.server.domain.product.ProductInfo
import kr.hhplus.be.server.domain.product.ProductOption

sealed class ProductResult {
    data class Detail(
        val product: Product,
        val options: List<ProductOption>
    )

    data class Popular(
        val productId: Long,
        val name: String,
        val basePrice: Int,
        val totalSales: Int
    ) {
        companion object {
            fun from(info: ProductInfo.Popular): Popular {
                return Popular(
                    productId = info.id,
                    name = info.name,
                    basePrice = info.basePrice,
                    totalSales = info.totalSales
                )
            }

            fun from(rank: ProductInfo.Rank, product: Product): Popular {
                return Popular(
                    productId = rank.productId,
                    name = product.name,
                    basePrice = product.basePrice,
                    totalSales = rank.totalSales
                )
            }
        }
    }
}

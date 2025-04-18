package kr.hhplus.be.server.interfaces.product

import kr.hhplus.be.server.application.product.ProductResult
import kr.hhplus.be.server.domain.product.Product
import kr.hhplus.be.server.domain.product.ProductOption

sealed class ProductResponse {
    data class Summary(
        val id: Long,
        val name: String,
        val basePrice: Int
    ) {
        companion object {
            fun from(product: Product): Summary {
                return Summary(
                    id = product.id,
                    name = product.name,
                    basePrice = product.basePrice
                )
            }
        }
    }

    data class Detail(
        val id: Long,
        val name: String,
        val basePrice: Int,
        val options: List<Option>
    ) {
        companion object {
            fun from(product: Product, options: List<ProductOption>): Detail {
                val mappedOptions = options.map { Option(it.optionName, it.extraPrice) }
                return Detail(
                    id = product.id,
                    name = product.name,
                    basePrice = product.basePrice,
                    options = mappedOptions
                )
            }
        }
    }

    data class Option(
        val name: String,
        val extraPrice: Int
    )

    data class Popular(
        val id: Long,
        val name: String,
        val basePrice: Int,
        val totalSales: Int
    ) {
        companion object {
            fun from(popular: ProductResult.Popular): Popular {
                return Popular(
                    id = popular.productId,
                    name = popular.name,
                    basePrice = popular.basePrice,
                    totalSales = popular.totalSales
                )
            }
        }
    }
}

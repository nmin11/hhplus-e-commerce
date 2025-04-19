package kr.hhplus.be.server.domain.product

import kr.hhplus.be.server.infrastructure.product.PopularProductRecord

sealed class ProductInfo {
    data class Popular(
        val id: Long,
        val name: String,
        val basePrice: Int,
        val totalSales: Int
    ) {
        companion object {
            fun from(record: PopularProductRecord): Popular {
                return Popular(
                    id = record.id,
                    name = record.name,
                    basePrice = record.basePrice,
                    totalSales = record.totalSales
                )
            }
        }
    }
}

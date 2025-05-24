package kr.hhplus.be.server.domain.product

import kr.hhplus.be.server.domain.order.Order
import kr.hhplus.be.server.domain.payment.PaymentCompletedEvent
import kr.hhplus.be.server.infrastructure.product.PopularProductRecord
import kr.hhplus.be.server.infrastructure.product.ProductRankRedisEntry

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

    data class Rank(
        val productId: Long,
        val totalSales: Int
    ) {
        companion object {
            fun from(entry: ProductRankRedisEntry): Rank {
                return Rank(
                    productId = entry.productId,
                    totalSales = entry.totalSales
                )
            }
        }
    }

    data class SalesIncrement(
        val productId: Long,
        val quantity: Int
    ) {
        companion object {
            fun from(item: PaymentCompletedEvent.Item): SalesIncrement {
                return SalesIncrement(
                    productId = item.productId,
                    quantity = item.quantity
                )
            }
        }
    }

    data class StockItem(
        val productOptionId: Long,
        val quantity: Int
    ) {
        companion object {
            fun from(order: Order): List<StockItem> {
                return order.orderItems.map {
                    StockItem(
                        productOptionId = it.productOption.id,
                        quantity = it.quantity
                    )
                }
            }
        }
    }
}

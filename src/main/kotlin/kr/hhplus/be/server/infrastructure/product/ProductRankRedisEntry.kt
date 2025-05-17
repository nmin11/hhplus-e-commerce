package kr.hhplus.be.server.infrastructure.product

data class ProductRankRedisEntry(
    val productId: Long,
    val totalSales: Int
)

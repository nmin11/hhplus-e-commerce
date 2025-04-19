package kr.hhplus.be.server.infrastructure.product

data class PopularProductRecord(
    val id: Long,
    val name: String,
    val basePrice: Int,
    val totalSales: Int
)

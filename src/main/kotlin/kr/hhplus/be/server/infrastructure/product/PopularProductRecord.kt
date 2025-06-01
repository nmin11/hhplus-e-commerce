package kr.hhplus.be.server.infrastructure.product

data class PopularProductRecord(
    val id: Long,
    val name: String,
    val basePrice: Int,
    val totalSales: Int
) {
    companion object {
        fun from(projection: PopularProductProjection): PopularProductRecord {
            return PopularProductRecord(
                id = projection.getId(),
                name = projection.getName(),
                basePrice = projection.getBasePrice(),
                totalSales = projection.getTotalSales()
            )
        }
    }
}

package kr.hhplus.be.server.domain.product

data class PopularProductCacheEntry(
    val id: Long = 0L,
    val name: String = "",
    val basePrice: Int = 0,
    val totalSales: Int = 0
) {
    fun toInfo(): ProductInfo.Popular =
        ProductInfo.Popular(id, name, basePrice, totalSales)

    companion object {
        fun from(domain: ProductInfo.Popular): PopularProductCacheEntry =
            PopularProductCacheEntry(domain.id, domain.name, domain.basePrice, domain.totalSales)
    }
}

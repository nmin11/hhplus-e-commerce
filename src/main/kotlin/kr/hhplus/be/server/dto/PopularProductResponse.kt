package kr.hhplus.be.server.dto

data class PopularProductResponse(
    val productId: Long,
    val name: String,
    val basePrice: Int,
    val salesCount: Int
)

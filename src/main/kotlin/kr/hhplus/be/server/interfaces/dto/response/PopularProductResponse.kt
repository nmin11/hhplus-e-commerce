package kr.hhplus.be.server.interfaces.dto.response

data class PopularProductResponse(
    val productId: Long,
    val name: String,
    val basePrice: Int,
    val salesCount: Int
)
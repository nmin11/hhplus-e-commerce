package kr.hhplus.be.server.dto

data class ProductDetailResponse(
    val id: Long,
    val name: String,
    val basePrice: Int,
    val options: List<ProductOptionResponse>
)

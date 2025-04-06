package kr.hhplus.be.server.interfaces.dto.response

data class ProductDetailResponse(
    val id: Long,
    val name: String,
    val basePrice: Int,
    val options: List<ProductOptionResponse>
)
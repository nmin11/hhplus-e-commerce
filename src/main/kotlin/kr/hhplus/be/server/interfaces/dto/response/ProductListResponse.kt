package kr.hhplus.be.server.interfaces.dto.response

data class ProductListResponse(
    val id: Long,
    val name: String,
    val basePrice: Int
)
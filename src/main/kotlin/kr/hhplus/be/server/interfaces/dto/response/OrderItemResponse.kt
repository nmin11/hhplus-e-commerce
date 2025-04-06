package kr.hhplus.be.server.interfaces.dto.response

data class OrderItemResponse(
    val productName: String,
    val optionName: String,
    val quantity: Int,
    val subtotalPrice: Int
)
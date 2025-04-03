package kr.hhplus.be.server.dto

data class OrderItemResponse(
    val productName: String,
    val optionName: String,
    val quantity: Int,
    val subtotalPrice: Int
)

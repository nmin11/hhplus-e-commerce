package kr.hhplus.be.server.interfaces.dto.response

data class BalanceHistoryResponse(
    val changeType: String,
    val changeAmount: Int,
    val totalAmount: Int,
    val createdAt: String
)
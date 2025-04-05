package kr.hhplus.be.server.dto

data class BalanceHistoryResponse(
    val changeType: String,
    val changeAmount: Int,
    val totalAmount: Int,
    val createdAt: String
)

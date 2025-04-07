package kr.hhplus.be.server.interfaces.balance

import kr.hhplus.be.server.domain.balance.Balance

sealed class BalanceResponse {
    data class Summary(
        val customerId: Long,
        val amount: Int
    )

    data class Histories(
        val changeType: String,
        val changeAmount: Int,
        val totalAmount: Int,
        val createdAt: String
    )

    companion object {
        fun from(balance: Balance): Summary {
            return Summary(
                customerId = balance.customerId,
                amount = balance.amount
            )
        }
    }
}

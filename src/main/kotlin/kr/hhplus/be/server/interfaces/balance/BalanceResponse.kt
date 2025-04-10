package kr.hhplus.be.server.interfaces.balance

import kr.hhplus.be.server.application.balance.BalanceResult

sealed class BalanceResponse {
    data class Summary(
        val customerId: Long,
        val amount: Int
    ) {
        companion object {
            fun from(result: BalanceResult.Summary): Summary {
                return Summary(
                    customerId = result.customerId,
                    amount = result.amount
                )
            }
        }
    }

    data class History(
        val changeType: String,
        val changeAmount: Int,
        val totalAmount: Int,
        val createdAt: String
    ) {
        companion object {
            fun from(result: BalanceResult.History): History {
                return History(
                    changeType = result.changeType,
                    changeAmount = result.changeAmount,
                    totalAmount = result.totalAmount,
                    createdAt = result.createdAt
                )
            }
        }
    }
}

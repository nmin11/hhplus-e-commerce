package kr.hhplus.be.server.interfaces.balance

import kr.hhplus.be.server.domain.balance.Balance
import kr.hhplus.be.server.domain.balance.BalanceHistory

sealed class BalanceResponse {
    data class Summary(
        val customerId: Long,
        val amount: Int
    ) {
        companion object {
            fun from(balance: Balance): Summary {
                return Summary(
                    customerId = balance.customer.requireSavedId(),
                    amount = balance.amount
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
            fun from(balanceHistory: BalanceHistory): History {
                return History(
                    changeType = balanceHistory.changeType.name,
                    changeAmount = balanceHistory.changeAmount,
                    totalAmount = balanceHistory.totalAmount,
                    createdAt = balanceHistory.createdAt.toString()
                )
            }
        }
    }
}

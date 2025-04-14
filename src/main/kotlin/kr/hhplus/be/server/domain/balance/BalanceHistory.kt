package kr.hhplus.be.server.domain.balance

import java.time.LocalDateTime

class BalanceHistory private constructor(
    val customerId: Long,
    val changeType: BalanceChangeType,
    val changeAmount: Int,
    val totalAmount: Int
) {
    val id: Long = 0L
    val createdAt: LocalDateTime = LocalDateTime.now()

    companion object {
        fun charge(customerId: Long, amount: Int, updatedAmount: Int): BalanceHistory {
            return BalanceHistory(
                customerId,
                changeType = BalanceChangeType.CHARGE,
                changeAmount = amount,
                totalAmount = updatedAmount
            )
        }

        fun use(customerId: Long, amount: Int, updatedAmount: Int): BalanceHistory {
            return BalanceHistory(
                customerId,
                changeType = BalanceChangeType.USE,
                changeAmount = amount,
                totalAmount = updatedAmount
            )
        }
    }
}

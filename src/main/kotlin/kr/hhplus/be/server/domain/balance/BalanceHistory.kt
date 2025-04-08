package kr.hhplus.be.server.domain.balance

import java.time.LocalDateTime

class BalanceHistory(
    val customerId: Long,
    val changeType: BalanceChangeType,
    val changeAmount: Int,
    val totalAmount: Int
) {
    var id: Long? = null
    val createdAt: LocalDateTime = LocalDateTime.now()
}

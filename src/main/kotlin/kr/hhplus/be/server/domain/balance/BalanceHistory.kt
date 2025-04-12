package kr.hhplus.be.server.domain.balance

import kr.hhplus.be.server.domain.customer.Customer
import java.time.LocalDateTime

class BalanceHistory(
    val customer: Customer,
    val changeType: BalanceChangeType,
    val changeAmount: Int,
    val totalAmount: Int
) {
    var id: Long? = null
    val createdAt: LocalDateTime = LocalDateTime.now()

    fun requireSavedId(): Long =
        id ?: throw IllegalStateException("BalanceHistory 객체가 저장되지 않았습니다.")
}

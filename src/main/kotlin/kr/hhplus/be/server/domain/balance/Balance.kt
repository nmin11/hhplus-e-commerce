package kr.hhplus.be.server.domain.balance

import java.time.LocalDateTime

class Balance(
    val customerId: Long,
    val amount: Int
) {
    val id: Long? = null
    val updatedAt: LocalDateTime = LocalDateTime.now()
}

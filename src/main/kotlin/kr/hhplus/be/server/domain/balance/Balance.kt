package kr.hhplus.be.server.domain.balance

import java.time.LocalDateTime

class Balance(
    val customerId: Long,
    var amount: Int
) {
    val id: Long? = null
    var updatedAt: LocalDateTime = LocalDateTime.now()
}

package kr.hhplus.be.server.domain.balance

import kr.hhplus.be.server.domain.customer.Customer
import java.time.LocalDateTime

class Balance(
    val customer: Customer,
    var amount: Int
) {
    var id: Long? = null
    var updatedAt: LocalDateTime = LocalDateTime.now()

    fun requireSavedId(): Long =
        id ?: throw IllegalStateException("Balance 객체가 저장되지 않았습니다.")
}

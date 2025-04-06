package kr.hhplus.be.server.domain.customer

import java.time.LocalDateTime

class Customer(
    val username: String
) {
    val id: Long? = null
    val createdAt: LocalDateTime = LocalDateTime.now()
    val updatedAt: LocalDateTime = LocalDateTime.now()
}

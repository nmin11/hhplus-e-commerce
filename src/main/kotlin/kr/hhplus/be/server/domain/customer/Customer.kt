package kr.hhplus.be.server.domain.customer

import java.time.LocalDateTime

class Customer private constructor(
    val username: String,
) {
    val id: Long = 0L
    val createdAt: LocalDateTime = LocalDateTime.now()
    var updatedAt: LocalDateTime = LocalDateTime.now()

    companion object {
        fun create(username: String): Customer {
            require(username.isNotBlank()) { "사용자 이름은 비어있을 수 없습니다." }
            return Customer(username)
        }
    }
}

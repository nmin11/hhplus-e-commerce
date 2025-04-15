package kr.hhplus.be.server.domain.customer

import jakarta.persistence.*
import kr.hhplus.be.server.domain.common.BaseEntity

@Entity
@Table(name = "customer")
class Customer private constructor(
    @Column(name = "username", nullable = false, length = 50)
    val username: String
) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L

    companion object {
        fun create(username: String): Customer {
            require(username.isNotBlank()) { "사용자 이름은 비어있을 수 없습니다." }
            return Customer(username)
        }
    }
}

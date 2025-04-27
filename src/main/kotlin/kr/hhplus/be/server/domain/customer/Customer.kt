package kr.hhplus.be.server.domain.customer

import jakarta.persistence.*
import kr.hhplus.be.server.domain.common.BaseEntity
import kr.hhplus.be.server.support.exception.customer.CustomerInvalidNameException

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
            if (username.isBlank()) throw CustomerInvalidNameException()
            return Customer(username)
        }
    }
}

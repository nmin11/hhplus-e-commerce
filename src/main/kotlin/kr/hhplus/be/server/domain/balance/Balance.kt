package kr.hhplus.be.server.domain.balance

import jakarta.persistence.*
import kr.hhplus.be.server.domain.common.BaseEntity
import kr.hhplus.be.server.domain.customer.Customer
import java.time.LocalDateTime

@Entity
@Table(name = "balance")
class Balance private constructor(
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false, unique = true)
    val customer: Customer,

    @Column(name = "amount", nullable = false)
    private var amount: Int
) : BaseEntity() {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L

    @Version
    var version: Long = 0L

    companion object {
        fun create(customer: Customer, amount: Int): Balance {
            return Balance(customer, amount)
        }
    }

    fun charge(amount: Int) {
        require(amount > 0) { "충전 금액은 0보다 커야 합니다." }
        this.amount += amount
    }

    fun deduct(amount: Int) {
        require(amount > 0) { "차감 금액은 0보다 커야 합니다." }
        if (this.amount < amount) throw IllegalStateException("잔액이 부족합니다.")
        this.amount -= amount
    }

    fun getAmount(): Int = amount
}

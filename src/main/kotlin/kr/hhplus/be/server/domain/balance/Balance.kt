package kr.hhplus.be.server.domain.balance

import jakarta.persistence.*
import kr.hhplus.be.server.domain.common.BaseEntity
import kr.hhplus.be.server.domain.customer.Customer
import kr.hhplus.be.server.support.exception.balance.BalanceInsufficientException
import kr.hhplus.be.server.support.exception.balance.BalanceInvalidAmountException

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
        if (amount <= 0) throw BalanceInvalidAmountException()
        this.amount += amount
    }

    fun deduct(amount: Int) {
        if (amount <= 0) throw BalanceInvalidAmountException()
        if (this.amount < amount) throw BalanceInsufficientException()
        this.amount -= amount
    }

    fun getAmount(): Int = amount
}

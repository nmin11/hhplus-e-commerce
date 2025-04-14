package kr.hhplus.be.server.domain.balance

import kr.hhplus.be.server.domain.customer.Customer
import java.time.LocalDateTime

class Balance private constructor(
    val customer: Customer,
    private var amount: Int
) {
    val id: Long = 0L
    private var updatedAt: LocalDateTime = LocalDateTime.now()

    companion object {
        fun create(customer: Customer, amount: Int): Balance {
            return Balance(customer, amount)
        }
    }

    fun charge(amount: Int) {
        require(amount > 0) { "충전 금액은 0보다 커야 합니다." }
        this.amount += amount
        updatedAt = LocalDateTime.now()
    }

    fun deduct(amount: Int) {
        require(amount > 0) { "차감 금액은 0보다 커야 합니다." }
        if (this.amount < amount) throw IllegalStateException("잔액이 부족합니다.")
        this.amount -= amount
        updatedAt = LocalDateTime.now()
    }

    fun getAmount(): Int = amount
}

package kr.hhplus.be.server.domain.customer

import kr.hhplus.be.server.domain.balance.Balance
import kr.hhplus.be.server.domain.balance.BalanceHistory
import kr.hhplus.be.server.domain.coupon.CustomerCoupon
import kr.hhplus.be.server.domain.order.Order
import kr.hhplus.be.server.domain.payment.Payment
import java.time.LocalDateTime

class Customer private constructor(
    val username: String,
) {
    var id: Long? = null
    val createdAt: LocalDateTime = LocalDateTime.now()
    var updatedAt: LocalDateTime = LocalDateTime.now()
    var balance: Balance? = null
    val balanceHistories: MutableList<BalanceHistory> = mutableListOf()
    val customerCoupons: MutableList<CustomerCoupon> = mutableListOf()
    val orders: MutableList<Order> = mutableListOf()
    val payments: MutableList<Payment> = mutableListOf()

    companion object {
        fun create(username: String): Customer {
            require(username.isNotBlank()) { "사용자 이름은 비어있을 수 없습니다." }
            return Customer(username)
        }
    }

    fun requireSavedId(): Long =
        id ?: throw IllegalStateException("Customer 객체가 저장되지 않았습니다.")
}

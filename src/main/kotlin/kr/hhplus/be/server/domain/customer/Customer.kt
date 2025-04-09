package kr.hhplus.be.server.domain.customer

import kr.hhplus.be.server.domain.balance.Balance
import kr.hhplus.be.server.domain.balance.BalanceHistory
import kr.hhplus.be.server.domain.coupon.CustomerCoupon
import kr.hhplus.be.server.domain.order.Order
import kr.hhplus.be.server.domain.payment.Payment
import java.time.LocalDateTime

class Customer(
    var username: String,
) {
    var id: Long? = null
    val createdAt: LocalDateTime = LocalDateTime.now()
    var updatedAt: LocalDateTime = LocalDateTime.now()
    var balance: Balance? = null
    var balanceHistories: MutableList<BalanceHistory> = mutableListOf()
    var customerCoupons: MutableList<CustomerCoupon> = mutableListOf()
    var orders: MutableList<Order> = mutableListOf()
    var payments: MutableList<Payment> = mutableListOf()
}

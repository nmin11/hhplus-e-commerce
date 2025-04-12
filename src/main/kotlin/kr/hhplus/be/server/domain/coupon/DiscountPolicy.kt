package kr.hhplus.be.server.domain.coupon

interface DiscountPolicy {
    fun getType(): String
    fun getAmount(): Int
    fun calculateDiscount(totalPrice: Int): Int
}

package kr.hhplus.be.server.domain.coupon

class FixedDiscountPolicy(private val amount: Int) : DiscountPolicy {
    override fun getType(): String = "FIXED"
    override fun getAmount(): Int = amount
    override fun calculateDiscount(totalPrice: Int): Int = amount
}

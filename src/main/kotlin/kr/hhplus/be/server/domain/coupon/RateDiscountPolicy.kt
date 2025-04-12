package kr.hhplus.be.server.domain.coupon

class RateDiscountPolicy(private val rate: Int) : DiscountPolicy {
    override fun getType(): String = "RATE"
    override fun getAmount(): Int = rate
    override fun calculateDiscount(totalPrice: Int): Int = (totalPrice * rate / 100.0).toInt()
}

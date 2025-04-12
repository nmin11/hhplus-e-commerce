package kr.hhplus.be.server.domain.coupon

interface CustomerCouponRepository {
    fun save(customerCoupon: CustomerCoupon): CustomerCoupon
    fun findByCustomerIdAndCouponId(customerId: Long, couponId: Long): CustomerCoupon?
}

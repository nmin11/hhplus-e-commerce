package kr.hhplus.be.server.domain.coupon

interface CustomerCouponRepository {
    fun save(customerCoupon: CustomerCoupon): CustomerCoupon
    fun saveAll(customerCoupons: List<CustomerCoupon>): List<CustomerCoupon>
    fun findAllByCustomerId(customerId: Long): List<CustomerCoupon>
    fun findByCustomerIdAndCouponId(customerId: Long, couponId: Long): CustomerCoupon?
    fun findAllByCouponIn(coupons: List<Coupon>): List<CustomerCoupon>
}

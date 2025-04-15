package kr.hhplus.be.server.infrastructure.coupon

import kr.hhplus.be.server.domain.coupon.Coupon
import kr.hhplus.be.server.domain.coupon.CustomerCoupon
import org.springframework.data.jpa.repository.JpaRepository

interface CustomerCouponJpaRepository : JpaRepository<CustomerCoupon, Long> {
    fun findAllByCustomerId(customerId: Long): List<CustomerCoupon>
    fun findByCustomerIdAndCouponId(customerId: Long, couponId: Long): CustomerCoupon?
    fun findAllByCouponIn(coupons: List<Coupon>): List<CustomerCoupon>
}

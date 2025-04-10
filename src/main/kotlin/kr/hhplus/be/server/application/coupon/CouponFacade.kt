package kr.hhplus.be.server.application.coupon

import kr.hhplus.be.server.domain.coupon.CouponService
import kr.hhplus.be.server.domain.coupon.CustomerCoupon
import kr.hhplus.be.server.domain.coupon.CustomerCouponService
import kr.hhplus.be.server.domain.customer.CustomerService
import org.springframework.stereotype.Component

@Component
class CouponFacade(
    private val couponService: CouponService,
    private val customerService: CustomerService,
    private val customerCouponService: CustomerCouponService
) {
    fun issueCouponToCustomer(couponId: Long, customerId: Long): CustomerCoupon {
        val customer = customerService.getById(customerId)
        val coupon = couponService.getById(couponId)

        // 쿠폰 중복 발급 여부 검사
        customerCouponService.validateNotIssued(customerId, couponId)

        // 쿠폰 수량 검사 및 차감
        couponService.decreaseQuantity(coupon)

        // 쿠폰 발급
        return customerCouponService.issue(customer, coupon)
    }
}

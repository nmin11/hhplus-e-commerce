package kr.hhplus.be.server.application.coupon

import kr.hhplus.be.server.domain.coupon.CouponService
import kr.hhplus.be.server.domain.coupon.CustomerCouponService
import kr.hhplus.be.server.domain.customer.CustomerService
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class CouponFacade(
    private val couponService: CouponService,
    private val customerService: CustomerService,
    private val customerCouponService: CustomerCouponService
) {
    @Transactional
    fun issueCouponToCustomer(command: CouponCommand.Issue): CouponResult.Issue {
        val (couponId, customerId) = command
        val customer = customerService.getById(customerId)
        val coupon = couponService.getById(couponId)

        // 쿠폰 사용 가능 기간 및 중복 발급 여부 검사
        coupon.validatePeriod()
        customerCouponService.validateNotIssued(customerId, couponId)

        // 쿠폰 수량 검사 및 차감
        couponService.decreaseQuantity(coupon)

        // 쿠폰 발급
        val customerCoupon = customerCouponService.issue(customerId, couponId)
        return CouponResult.Issue.from(customerCoupon)
    }

    fun getCustomerCoupons(customerId: Long): List<CouponResult.OwnedCoupon> {
        val customerCoupons = customerCouponService.getAllByCustomerId(customerId)
        return customerCoupons.map {
            val coupon = couponService.getById(it.couponId)
            CouponResult.OwnedCoupon.from(it, coupon)
        }
    }
}

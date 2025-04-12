package kr.hhplus.be.server.application.coupon

import kr.hhplus.be.server.domain.coupon.CouponService
import kr.hhplus.be.server.domain.coupon.CustomerCoupon
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
        val customer = customerService.getById(command.customerId)
        val coupon = couponService.getById(command.couponId)

        // 쿠폰 중복 발급 여부 검사
        customerCouponService.validateNotIssued(command.customerId, command.couponId)

        // 쿠폰 수량 검사 및 차감
        couponService.decreaseQuantity(coupon)

        // 쿠폰 발급
        val customerCoupon = customerCouponService.issue(customer, coupon)
        return CouponResult.Issue.from(customerCoupon)
    }
}

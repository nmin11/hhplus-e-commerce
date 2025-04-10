package kr.hhplus.be.server.domain.coupon

import org.springframework.stereotype.Service

@Service
class CustomerCouponService(
    private val customerCouponRepository: CustomerCouponRepository
) {
    fun validateIssuedCoupon(customerId: Long, couponId: Long): CustomerCoupon {
        val customerCoupon = customerCouponRepository.findByCustomerIdAndCouponId(customerId, couponId)
            ?: throw IllegalArgumentException("해당 쿠폰은 고객에게 발급되지 않았습니다.")

        if (customerCoupon.status == CustomerCouponStatus.USED) {
            throw IllegalStateException("이미 사용된 쿠폰입니다.")
        } else if (customerCoupon.status == CustomerCouponStatus.EXPIRED) {
            throw IllegalStateException("사용 기간이 만료된 쿠폰입니다.")
        }

        return customerCoupon
    }
}

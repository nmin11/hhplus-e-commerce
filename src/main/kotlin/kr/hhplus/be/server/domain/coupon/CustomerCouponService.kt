package kr.hhplus.be.server.domain.coupon

import org.springframework.stereotype.Service

@Service
class CustomerCouponService(
    private val customerCouponRepository: CustomerCouponRepository
) {
    fun getByCustomerIdAndCouponId(customerId: Long, couponId: Long): CustomerCoupon {
        return customerCouponRepository.findByCustomerIdAndCouponId(customerId, couponId)
            ?: throw IllegalArgumentException("해당 쿠폰은 고객에게 발급되지 않았습니다.")
    }
}

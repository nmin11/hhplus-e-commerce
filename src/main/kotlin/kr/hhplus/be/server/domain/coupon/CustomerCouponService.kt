package kr.hhplus.be.server.domain.coupon

import kr.hhplus.be.server.domain.customer.Customer
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CustomerCouponService(
    private val customerCouponRepository: CustomerCouponRepository
) {
    fun getAllByCustomerId(customerId: Long): List<CustomerCoupon> {
        return customerCouponRepository.findAllByCustomerId(customerId)
    }

    fun validateIssuedCoupon(customerId: Long, couponId: Long): CustomerCoupon {
        val customerCoupon = customerCouponRepository.findByCustomerIdAndCouponId(customerId, couponId)
            ?: throw IllegalArgumentException("해당 쿠폰은 고객에게 발급되지 않았습니다.")

        return customerCoupon.validateUsable()
    }

    fun validateNotIssued(customerId: Long, couponId: Long) {
        val existing = customerCouponRepository.findByCustomerIdAndCouponId(customerId, couponId)
        if (existing != null) {
            throw IllegalStateException("해당 쿠폰은 이미 발급된 쿠폰입니다.")
        }
    }

    fun issue(customerId: Long, couponId: Long): CustomerCoupon {
        val customerCoupon = CustomerCoupon.issue(customerId, customerId)
        return customerCouponRepository.save(customerCoupon)
    }

    @Transactional
    fun updateAsExpired(coupons: List<Coupon>) {
        val expiredCustomerCoupons = customerCouponRepository.findAllByCouponIn(coupons)
        expiredCustomerCoupons.forEach { it.expireIfAvailable() }
        customerCouponRepository.saveAll(expiredCustomerCoupons)
    }
}

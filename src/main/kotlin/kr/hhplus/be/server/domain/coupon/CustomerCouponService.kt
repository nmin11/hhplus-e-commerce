package kr.hhplus.be.server.domain.coupon

import kr.hhplus.be.server.domain.customer.Customer
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.orm.ObjectOptimisticLockingFailureException
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

    fun issue(customer: Customer, coupon: Coupon): CustomerCoupon {
        try {
            val customerCoupon = CustomerCoupon.issue(customer, coupon)
            return customerCouponRepository.save(customerCoupon)
        } catch (_: DataIntegrityViolationException) {
            throw IllegalStateException("해당 쿠폰은 이미 발급된 쿠폰입니다.")
        }
    }

    @Transactional
    fun updateAsExpired(coupons: List<Coupon>) {
        val expiredCustomerCoupons = customerCouponRepository.findAllByCouponIn(coupons)
        expiredCustomerCoupons.forEach { it.expireIfAvailable() }
        customerCouponRepository.saveAll(expiredCustomerCoupons)
    }

    @Transactional
    fun markAsUsed(customerCoupon: CustomerCoupon) {
        try {
            customerCoupon.markAsUsed()
            customerCouponRepository.saveAndFlush(customerCoupon)
        } catch (_: ObjectOptimisticLockingFailureException) {
            throw IllegalStateException("지금은 결제를 진행할 수 없습니다. 잠시 후 다시 시도해주세요.")
        }
    }
}

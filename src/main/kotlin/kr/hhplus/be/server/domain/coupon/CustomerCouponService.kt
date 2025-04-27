package kr.hhplus.be.server.domain.coupon

import kr.hhplus.be.server.domain.customer.Customer
import kr.hhplus.be.server.support.exception.coupon.CustomerCouponAlreadyIssuedException
import kr.hhplus.be.server.support.exception.coupon.CustomerCouponConflictException
import kr.hhplus.be.server.support.exception.coupon.CustomerCouponNotFoundException
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
            ?: throw CustomerCouponNotFoundException()

        return customerCoupon.validateUsable()
    }

    fun issue(customer: Customer, coupon: Coupon): CustomerCoupon {
        try {
            val customerCoupon = CustomerCoupon.issue(customer, coupon)
            return customerCouponRepository.save(customerCoupon)
        } catch (_: DataIntegrityViolationException) {
            throw CustomerCouponAlreadyIssuedException()
        }
    }

    @Transactional
    fun updateAsExpired(coupons: List<Coupon>) {
        val expiredCustomerCoupons = customerCouponRepository.findAllByCouponIn(coupons)
        expiredCustomerCoupons.forEach { it.expireIfAvailable() }
        customerCouponRepository.saveAll(expiredCustomerCoupons)
    }

    fun markAsUsed(customerCoupon: CustomerCoupon) {
        try {
            customerCoupon.markAsUsed()
            customerCouponRepository.saveAndFlush(customerCoupon)
        } catch (_: ObjectOptimisticLockingFailureException) {
            throw CustomerCouponConflictException()
        }
    }
}

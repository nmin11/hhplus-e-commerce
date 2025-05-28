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

    fun getIssuedCoupon(customerId: Long, couponId: Long): CustomerCoupon {
        return customerCouponRepository.findByCustomerIdAndCouponId(customerId, couponId)
            ?: throw CustomerCouponNotFoundException()
    }

    fun validateIssuedCoupon(customerId: Long, couponId: Long): CustomerCoupon {
        val customerCoupon = getIssuedCoupon(customerId, couponId)
        return customerCoupon.validateUsable()
    }

    @Transactional
    fun issue(customer: Customer, coupon: Coupon): CustomerCoupon {
        try {
            val customerCoupon = CustomerCoupon.issue(customer, coupon)
            return customerCouponRepository.save(customerCoupon)
        } catch (_: DataIntegrityViolationException) {
            throw CustomerCouponAlreadyIssuedException()
        }
    }

    @Transactional
    fun issueAll(customerCoupons: List<CustomerCoupon>) {
        customerCouponRepository.saveAll(customerCoupons)
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
            throw CustomerCouponConflictException()
        }
    }

    @Transactional
    fun rollbackUse(customerCoupon: CustomerCoupon) {
        customerCoupon.rollbackUse()
        customerCouponRepository.save(customerCoupon)
    }
}

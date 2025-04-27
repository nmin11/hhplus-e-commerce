package kr.hhplus.be.server.infrastructure.coupon

import kr.hhplus.be.server.domain.coupon.Coupon
import kr.hhplus.be.server.domain.coupon.CustomerCoupon
import kr.hhplus.be.server.domain.coupon.CustomerCouponRepository
import org.springframework.stereotype.Repository

@Repository
class CustomerCouponRepositoryImpl(
    private val customerCouponJpaRepository: CustomerCouponJpaRepository
) : CustomerCouponRepository {
    override fun save(customerCoupon: CustomerCoupon): CustomerCoupon {
        return customerCouponJpaRepository.save(customerCoupon)
    }

    override fun saveAll(customerCoupons: List<CustomerCoupon>): List<CustomerCoupon> {
        return customerCouponJpaRepository.saveAll(customerCoupons)
    }

    override fun saveAndFlush(customerCoupon: CustomerCoupon): CustomerCoupon {
        return customerCouponJpaRepository.saveAndFlush(customerCoupon)
    }

    override fun findAllByCustomerId(customerId: Long): List<CustomerCoupon> {
        return customerCouponJpaRepository.findAllByCustomerId(customerId)
    }

    override fun findByCustomerIdAndCouponId(customerId: Long, couponId: Long): CustomerCoupon? {
        return customerCouponJpaRepository.findByCustomerIdAndCouponId(customerId, couponId)
    }

    override fun findAllByCouponIn(coupons: List<Coupon>): List<CustomerCoupon> {
        return customerCouponJpaRepository.findAllByCouponIn(coupons)
    }
}

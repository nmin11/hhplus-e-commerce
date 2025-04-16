package kr.hhplus.be.server.application.coupon

import kr.hhplus.be.server.domain.coupon.Coupon
import kr.hhplus.be.server.domain.coupon.CouponRepository
import kr.hhplus.be.server.domain.coupon.CustomerCouponRepository
import kr.hhplus.be.server.domain.customer.Customer
import kr.hhplus.be.server.domain.customer.CustomerRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CouponFacadeIntegrationTest @Autowired constructor(
    private val couponFacade: CouponFacade,
    private val customerRepository: CustomerRepository,
    private val couponRepository: CouponRepository,
    private val customerCouponRepository: CustomerCouponRepository
) {
    private lateinit var customer: Customer
    private lateinit var coupon: Coupon

    @BeforeEach
    fun setup() {
        customer = Customer.create("coupon-user")
        customerRepository.save(customer)

        coupon = Coupon.createFixedDiscount(
            name = "test-coupon",
            amount = 1000,
            quantity = 10,
            startedAt = LocalDate.now().minusDays(1),
            expiredAt = LocalDate.now().plusDays(7)
        )
        couponRepository.save(coupon)
    }

    @Test
    @DisplayName("사용자에게 쿠폰 정상 발급")
    fun issueCoupon_shouldCreateCustomerCoupon() {
        // given
        val command = CouponCommand.Issue(customerId = customer.id, couponId = coupon.id)

        // when
        val result = couponFacade.issueCouponToCustomer(command)

        // then
        assertThat(result.customerId).isEqualTo(customer.id)
        assertThat(result.couponId).isEqualTo(coupon.id)

        val customerCoupon = customerCouponRepository.findByCustomerIdAndCouponId(customer.id, coupon.id)
        assertThat(customerCoupon).isNotNull
        assertThat(customerCoupon!!.status.name).isEqualTo("AVAILABLE")
    }
}

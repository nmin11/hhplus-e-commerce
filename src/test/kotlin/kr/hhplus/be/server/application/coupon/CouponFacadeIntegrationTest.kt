package kr.hhplus.be.server.application.coupon

import kr.hhplus.be.server.domain.coupon.Coupon
import kr.hhplus.be.server.domain.coupon.CouponRepository
import kr.hhplus.be.server.domain.coupon.CustomerCoupon
import kr.hhplus.be.server.domain.coupon.CustomerCouponRepository
import kr.hhplus.be.server.domain.customer.Customer
import kr.hhplus.be.server.domain.customer.CustomerRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CouponFacadeIntegrationTest @Autowired constructor(
    private val couponFacade: CouponFacade,
    private val customerRepository: CustomerRepository,
    private val couponRepository: CouponRepository,
    private val customerCouponRepository: CustomerCouponRepository
) {
    private lateinit var customer: Customer
    private lateinit var coupon: Coupon

    @BeforeAll
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

    @Test
    @DisplayName("쿠폰 수량이 0이면 쿠폰 발급 실패")
    fun issueCoupon_shouldFail_whenCouponQuantityExhausted() {
        // given
        val exhaustedCoupon = Coupon.createFixedDiscount(
            name = "0-count-coupon",
            amount = 2000,
            quantity = 0, // 쿠폰이 남아있지 않음
            startedAt = LocalDate.now().minusDays(1),
            expiredAt = LocalDate.now().plusDays(7)
        )
        couponRepository.save(exhaustedCoupon)

        val command = CouponCommand.Issue(
            customerId = customer.id,
            couponId = exhaustedCoupon.id
        )

        // when
        val exception = assertThrows<IllegalStateException> {
            couponFacade.issueCouponToCustomer(command)
        }

        // then
        assertThat(exception.message).isEqualTo("쿠폰 수량이 모두 소진되었습니다.")
    }

    @Test
    @DisplayName("쿠폰이 만료되었으면 발급 실패")
    fun issueCoupon_shouldFail_whenCouponIsExpired() {
        // given
        val expiredCoupon = Coupon.createFixedDiscount(
            name = "expired-coupon",
            amount = 3000,
            quantity = 10,
            startedAt = LocalDate.now().minusDays(10),
            expiredAt = LocalDate.now().minusDays(1) // 이미 만료됨
        )
        couponRepository.save(expiredCoupon)

        val command = CouponCommand.Issue(
            customerId = customer.id,
            couponId = expiredCoupon.id
        )

        // when
        val exception = assertThrows<IllegalStateException> {
            couponFacade.issueCouponToCustomer(command)
        }

        // then
        assertThat(exception.message).isEqualTo("유효하지 않은 쿠폰입니다.")
    }

    @Test
    @DisplayName("이미 발급받은 쿠폰이면 쿠폰 발급 실패")
    fun issueCoupon_shouldFail_whenAlreadyIssued() {
        // given
        val duplicateCoupon = Coupon.createFixedDiscount(
            name = "already-has-coupon",
            amount = 1000,
            quantity = 10,
            startedAt = LocalDate.now().minusDays(1),
            expiredAt = LocalDate.now().plusDays(7)
        )
        couponRepository.save(duplicateCoupon)

        val customerCoupon = CustomerCoupon.issue(customer, duplicateCoupon)
        customerCouponRepository.save(customerCoupon)

        val command = CouponCommand.Issue(
            customerId = customer.id,
            couponId = duplicateCoupon.id
        )

        // when
        val exception = assertThrows<IllegalStateException> {
            couponFacade.issueCouponToCustomer(command)
        }

        // then
        assertThat(exception.message).isEqualTo("해당 쿠폰은 이미 발급된 쿠폰입니다.")
    }
}

package kr.hhplus.be.server.domain.coupon

import kr.hhplus.be.server.domain.customer.Customer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate

class CustomerCouponTest {
    private val customer = Customer.create("tester")
    private val coupon = Coupon.createFixedDiscount(
        name = "10% 할인 쿠폰",
        amount = 1000,
        quantity = 100,
        startedAt = LocalDate.now().minusDays(1),
        expiredAt = LocalDate.now().plusDays(1)
    )

    @Nested
    inner class Issue {
        @Test
        @DisplayName("쿠폰 발급 시 AVAILABLE 상태로 생성된다")
        fun shouldCreateCouponWithAvailableStatus() {
            // when
            val customerCoupon = CustomerCoupon.issue(customer, coupon)

            // then
            assertThat(customerCoupon.customer).isEqualTo(customer)
            assertThat(customerCoupon.coupon).isEqualTo(coupon)
            assertThat(customerCoupon.status).isEqualTo(CustomerCouponStatus.AVAILABLE)
        }
    }

    @Nested
    inner class ExpireIfAvailable {
        @Test
        @DisplayName("AVAILABLE 상태이면 EXPIRED 상태로 변경된다")
        fun shouldExpireAvailableCoupon() {
            // given
            val customerCoupon = CustomerCoupon.issue(customer, coupon)

            // when
            customerCoupon.expireIfAvailable()

            // then
            assertThat(customerCoupon.status).isEqualTo(CustomerCouponStatus.EXPIRED)
        }

        @Test
        @DisplayName("USED 상태이면 변경되지 않는다")
        fun shouldNotExpireUsedCoupon() {
            // given
            val customerCoupon = CustomerCoupon.issue(customer, coupon).apply {
                status = CustomerCouponStatus.USED
            }

            // when
            customerCoupon.expireIfAvailable()

            // then
            assertThat(customerCoupon.status).isEqualTo(CustomerCouponStatus.USED)
        }

        @Test
        @DisplayName("EXPIRED 상태이면 변경되지 않는다")
        fun shouldNotExpireAlreadyExpiredCoupon() {
            // given
            val customerCoupon = CustomerCoupon.issue(customer, coupon).apply {
                status = CustomerCouponStatus.EXPIRED
            }

            // when
            customerCoupon.expireIfAvailable()

            // then
            assertThat(customerCoupon.status).isEqualTo(CustomerCouponStatus.EXPIRED)
        }
    }

    @Nested
    inner class ValidateUsable {
        @Test
        @DisplayName("AVAILABLE 상태이면 반환된다")
        fun shouldReturnCouponIfUsable() {
            // given
            val customerCoupon = CustomerCoupon.issue(customer, coupon)

            // when
            val result = customerCoupon.validateUsable()

            // then
            assertThat(result).isEqualTo(customerCoupon)
        }

        @Test
        @DisplayName("USED 상태이면 예외 발생")
        fun shouldThrowIfUsed() {
            // given
            val customerCoupon = CustomerCoupon.issue(customer, coupon).apply {
                status = CustomerCouponStatus.USED
            }

            // when
            val exception = assertThrows<IllegalStateException> {
                customerCoupon.validateUsable()
            }

            // then
            assertThat(exception.message).isEqualTo("이미 사용된 쿠폰입니다.")
        }

        @Test
        @DisplayName("EXPIRED 상태이면 예외 발생")
        fun shouldThrowIfExpired() {
            // given
            val customerCoupon = CustomerCoupon.issue(customer, coupon).apply {
                status = CustomerCouponStatus.EXPIRED
            }

            // when
            val exception = assertThrows<IllegalStateException> {
                customerCoupon.validateUsable()
            }

            // then
            assertThat(exception.message).isEqualTo("사용 기간이 만료된 쿠폰입니다.")
        }
    }
}

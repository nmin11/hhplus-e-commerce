package kr.hhplus.be.server.domain.coupon

import io.mockk.spyk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CustomerCouponTest {
    @Nested
    inner class Issue {
        private val customerId = 1L
        private val couponId = 1L

        @Test
        @DisplayName("쿠폰 발급 시 AVAILABLE 상태로 생성된다")
        fun shouldCreateCouponWithAvailableStatus() {
            // when
            val customerCoupon = spyk(CustomerCoupon.issue(customerId, couponId))

            // then
            assertThat(customerCoupon.status).isEqualTo(CustomerCouponStatus.AVAILABLE)
        }
    }

    @Nested
    inner class ExpireIfAvailable {
        private val customerId = 1L
        private val couponId = 1L

        @Test
        @DisplayName("AVAILABLE 상태이면 EXPIRED 상태로 변경된다")
        fun shouldExpireAvailableCoupon() {
            // given
            val customerCoupon = CustomerCoupon.issue(customerId, couponId)

            // when
            customerCoupon.expireIfAvailable()

            // then
            assertThat(customerCoupon.status).isEqualTo(CustomerCouponStatus.EXPIRED)
        }

        @Test
        @DisplayName("USED 상태이면 변경되지 않는다")
        fun shouldNotExpireUsedCoupon() {
            // given
            val customerCoupon = CustomerCoupon.issue(customerId, couponId).apply {
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
            val customerCoupon = CustomerCoupon.issue(customerId, couponId).apply {
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
        private val customerId = 1L
        private val couponId = 1L

        @Test
        @DisplayName("AVAILABLE 상태이면 반환된다")
        fun shouldReturnCouponIfUsable() {
            // given
            val customerCoupon = CustomerCoupon.issue(customerId, couponId)

            // when
            val result = customerCoupon.validateUsable()

            // then
            assertThat(result).isEqualTo(customerCoupon)
        }

        @Test
        @DisplayName("USED 상태이면 예외 발생")
        fun shouldThrowIfUsed() {
            // given
            val customerCoupon = CustomerCoupon.issue(customerId, couponId).apply {
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
            val customerCoupon = CustomerCoupon.issue(customerId, couponId).apply {
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

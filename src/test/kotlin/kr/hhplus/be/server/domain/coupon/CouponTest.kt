package kr.hhplus.be.server.domain.coupon

import kr.hhplus.be.server.support.exception.coupon.CouponInsufficientException
import kr.hhplus.be.server.support.exception.coupon.CouponInvalidPeriodException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate

class CouponTest {
    private val startedAt = LocalDate.now().minusDays(1)
    private val expiredAt = LocalDate.now().plusDays(1)

    @Nested
    inner class Create {
        @Test
        @DisplayName("정액 할인 쿠폰을 생성할 수 있다")
        fun createFixedDiscountCoupon() {
            val coupon = Coupon.createFixedDiscount(
                name = "5천원 할인",
                amount = 5000,
                quantity = 100,
                startedAt = startedAt,
                expiredAt = expiredAt
            )

            assertThat(coupon).isNotNull
            assertThat(coupon.totalQuantity).isEqualTo(100)
        }

        @Test
        @DisplayName("정률 할인 쿠폰을 생성할 수 있다")
        fun createRateDiscountCoupon() {
            val coupon = Coupon.createRateDiscount(
                name = "10% 할인",
                rate = 10,
                quantity = 100,
                startedAt = startedAt,
                expiredAt = expiredAt
            )

            assertThat(coupon).isNotNull
            assertThat(coupon.totalQuantity).isEqualTo(100)
        }
    }

    @Nested
    inner class DecreaseQuantity {
        @Test
        @DisplayName("수량이 남아 있으면 차감된다")
        fun decrease_whenQuantityAvailable() {
            val coupon = Coupon.createFixedDiscount(
                name = "테스트 쿠폰",
                amount = 1000,
                quantity = 2,
                startedAt = startedAt,
                expiredAt = expiredAt
            )

            coupon.decreaseQuantity()
        }

        @Test
        @DisplayName("수량이 없으면 예외 발생")
        fun throw_whenNoQuantity() {
            // given
            val coupon = Coupon.createFixedDiscount(
                name = "수량 0 쿠폰",
                amount = 1000,
                quantity = 0,
                startedAt = startedAt,
                expiredAt = expiredAt
            )

            // when
            val exception = assertThrows<CouponInsufficientException> {
                coupon.decreaseQuantity()
            }

            // then
            assertThat(exception).hasMessage("쿠폰 수량이 모두 소진되었습니다.")
        }
    }

    @Nested
    inner class CalculateDiscount {
        @Test
        @DisplayName("정액 할인 쿠폰은 고정 금액을 반환한다")
        fun fixedDiscount_shouldReturnFixedAmount() {
            // given
            val coupon = Coupon.createFixedDiscount(
                name = "고정 할인",
                amount = 3000,
                quantity = 100,
                startedAt = startedAt,
                expiredAt = expiredAt
            )

            // when
            val result = coupon.calculateDiscount(totalPrice = 100_000)

            // then
            assertThat(result).isEqualTo(3000)
        }

        @Test
        @DisplayName("정률 할인 쿠폰은 비율만큼 금액을 반환한다")
        fun rateDiscount_shouldReturnCalculatedAmount() {
            // given
            val coupon = Coupon.createRateDiscount(
                name = "10% 할인",
                rate = 10,
                quantity = 100,
                startedAt = startedAt,
                expiredAt = expiredAt
            )

            // when
            val result = coupon.calculateDiscount(totalPrice = 80_000)

            // then
            assertThat(result).isEqualTo(8000)
        }

        @Test
        @DisplayName("유효기간 밖이면 예외 발생")
        fun throw_whenCouponIsExpired() {
            // given
            val coupon = Coupon.createFixedDiscount(
                name = "기간 만료 쿠폰",
                amount = 1000,
                quantity = 100,
                startedAt = LocalDate.now().minusDays(10),
                expiredAt = LocalDate.now().minusDays(1)
            )

            // when
            val exception = assertThrows<CouponInvalidPeriodException> {
                coupon.calculateDiscount(10_000)
            }

            // then
            assertThat(exception).hasMessage("유효하지 않은 쿠폰입니다.")
        }
    }

    @Nested
    inner class ValidatePeriod {
        @Test
        @DisplayName("유효기간 안에 있으면 통과")
        fun validateWithinPeriod() {
            val coupon = Coupon.createFixedDiscount(
                name = "정상 쿠폰",
                amount = 1000,
                quantity = 100,
                startedAt = startedAt,
                expiredAt = expiredAt
            )

            coupon.validatePeriod()
        }

        @Test
        @DisplayName("유효기간 밖이면 예외 발생")
        fun validateOutsidePeriod_shouldThrow() {
            // given
            val coupon = Coupon.createFixedDiscount(
                name = "만료 쿠폰",
                amount = 1000,
                quantity = 100,
                startedAt = LocalDate.now().minusDays(10),
                expiredAt = LocalDate.now().minusDays(1)
            )

            // when
            val exception = assertThrows<CouponInvalidPeriodException> {
                coupon.validatePeriod()
            }

            // then
            assertThat(exception).hasMessage("유효하지 않은 쿠폰입니다.")
        }
    }
}

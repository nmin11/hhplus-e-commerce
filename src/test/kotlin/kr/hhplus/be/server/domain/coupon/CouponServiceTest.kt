package kr.hhplus.be.server.domain.coupon

import io.mockk.every
import io.mockk.mockk
import kr.hhplus.be.server.domain.customer.Customer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime

class CouponServiceTest {
    private val couponRepository = mockk<CouponRepository>()
    private val couponService = CouponService(couponRepository)

    @Nested
    inner class GetById {
        @Test
        @DisplayName("쿠폰 ID로 쿠폰을 조회하여 반환")
        fun returnCoupon_whenExists() {
            // given
            val couponId = 1L
            val expectedCoupon = Coupon(
                name = "할인쿠폰",
                discountType = DiscountType.FIXED,
                discountAmount = 1000,
                currentQuantity = 10,
                totalQuantity = 100,
                startedAt = LocalDateTime.now().minusDays(1),
                expiredAt = LocalDateTime.now().plusDays(1)
            ).apply { id = couponId }

            every { couponRepository.findById(couponId) } returns expectedCoupon

            // when
            val result = couponService.getById(couponId)

            // then
            assertThat(result).isEqualTo(expectedCoupon)
        }

        @Test
        @DisplayName("쿠폰이 존재하지 않을 경우 예외 발생")
        fun throwException_whenCouponNotFound() {
            // given
            every { couponRepository.findById(any()) } returns null

            // when
            val exception = assertThrows<IllegalArgumentException> {
                couponService.getById(999L)
            }

            // then
            assertThat(exception)
                .hasMessage("쿠폰 정보가 존재하지 않습니다.")
        }
    }

    @Nested
    inner class CalculateDiscount {
        private val now = LocalDateTime.now()

        @Test
        @DisplayName("정액 할인 쿠폰일 경우 할인 금액 반환")
        fun returnFixedDiscountAmount_whenCouponIsFixed() {
            // given
            val coupon = Coupon(
                name = "5천원 할인",
                discountType = DiscountType.FIXED,
                discountAmount = 5000,
                currentQuantity = 10,
                totalQuantity = 100,
                startedAt = now.minusDays(1),
                expiredAt = now.plusDays(1)
            )

            // when
            val result = couponService.calculateDiscount(coupon, totalPrice = 10000)

            // then
            assertThat(result).isEqualTo(5000)
        }

        @Test
        @DisplayName("퍼센트 할인 쿠폰일 경우 비율에 따른 할인 금액 반환")
        fun returnRateDiscountAmount_whenCouponIsRate() {
            // given
            val coupon = Coupon(
                name = "10% 할인",
                discountType = DiscountType.RATE,
                discountAmount = 10,
                currentQuantity = 10,
                totalQuantity = 100,
                startedAt = now.minusDays(1),
                expiredAt = now.plusDays(1)
            )

            // when
            val result = couponService.calculateDiscount(coupon, totalPrice = 20000)

            // then
            assertThat(result).isEqualTo(2000)
        }

        @Test
        @DisplayName("유효기간이 지난 쿠폰일 경우 예외 발생")
        fun throwException_whenCouponIsExpired() {
            // given
            val coupon = Coupon(
                name = "만료된 쿠폰",
                discountType = DiscountType.FIXED,
                discountAmount = 3000,
                currentQuantity = 10,
                totalQuantity = 100,
                startedAt = now.minusDays(10),
                expiredAt = now.minusDays(1)
            )

            // when
            val exception = assertThrows<IllegalStateException> {
                couponService.calculateDiscount(coupon, totalPrice = 10000)
            }

            // then
            assertThat(exception).hasMessage("유효하지 않은 쿠폰입니다.")
        }

        @Test
        @DisplayName("시작일이 아직 안 된 쿠폰일 경우 예외 발생")
        fun throwException_whenCouponNotYetStarted() {
            // given
            val coupon = Coupon(
                name = "예약된 쿠폰",
                discountType = DiscountType.FIXED,
                discountAmount = 3000,
                currentQuantity = 10,
                totalQuantity = 100,
                startedAt = now.plusDays(1),
                expiredAt = now.plusDays(10)
            )

            // when
            val exception = assertThrows<IllegalStateException> {
                couponService.calculateDiscount(coupon, totalPrice = 10000)
            }

            // then
            assertThat(exception).hasMessage("유효하지 않은 쿠폰입니다.")
        }
    }
}

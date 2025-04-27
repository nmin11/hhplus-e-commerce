package kr.hhplus.be.server.domain.coupon

import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate

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
            val expectedCoupon = Coupon.createFixedDiscount(
                name = "할인쿠폰",
                amount = 1000,
                quantity = 100,
                startedAt = LocalDate.now().minusDays(1),
                expiredAt = LocalDate.now().plusDays(1)
            )

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
    inner class DecreaseQuantity {
        @Test
        @DisplayName("쿠폰 수량이 남아 있는 경우 수량을 1 줄이고 저장")
        fun decreaseQuantity_shouldReduceAndSave() {
            // given
            val coupon = mockk<Coupon>(relaxed = true)

            every { coupon.decreaseQuantity() } just Runs
            every { couponRepository.save(coupon) } returns coupon

            // when
            couponService.decreaseQuantity(coupon)

            // then
            verify(exactly = 1) { coupon.decreaseQuantity() }
        }
    }

    @Nested
    inner class GetExpiredCoupons {
        @Test
        @DisplayName("지정한 기준 날짜 이전에 만료된 쿠폰들을 반환")
        fun shouldReturnExpiredCoupons() {
            // given
            val today = LocalDate.of(2025, 4, 11)
            val expiredCoupons = listOf(
                Coupon.createFixedDiscount(
                    name = "첫 구매 할인",
                    amount = 1000,
                    quantity = 100,
                    startedAt = today.minusDays(10),
                    expiredAt = today.minusDays(1)
                ),
                Coupon.createRateDiscount(
                    name = "봄맞이 할인",
                    rate = 20,
                    quantity = 50,
                    startedAt = today.minusDays(30),
                    expiredAt = today.minusDays(5)
                )
            )

            every { couponRepository.findAllByExpiredAtBefore(today) } returns expiredCoupons

            // when
            val result = couponService.getExpiredCoupons(today)

            // then
            assertThat(result).isEqualTo(expiredCoupons)
            verify(exactly = 1) { couponRepository.findAllByExpiredAtBefore(today) }
        }
    }
}

package kr.hhplus.be.server.domain.coupon

import io.mockk.*
import kr.hhplus.be.server.infrastructure.coupon.CouponIssueResult
import kr.hhplus.be.server.support.exception.coupon.CouponInsufficientException
import kr.hhplus.be.server.support.exception.coupon.CouponIssueFailedException
import kr.hhplus.be.server.support.exception.coupon.CouponNotFoundException
import kr.hhplus.be.server.support.exception.coupon.CustomerCouponAlreadyIssuedException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
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
            val exception = assertThrows<CouponNotFoundException> {
                couponService.getById(999L)
            }

            // then
            assertThat(exception)
                .hasMessage("쿠폰 정보가 존재하지 않습니다.")
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
    inner class Issue {
        private val couponId = 1L
        private val customerId = 100L

        private fun mockCoupon(): Coupon {
            val coupon = mockk<Coupon>(relaxed = true)
            every { coupon.id } returns couponId
            every { coupon.expiredAt } returns LocalDate.now().plusDays(1)
            return coupon
        }

        @Test
        @DisplayName("쿠폰 발급 성공 시 예외 없이 통과")
        fun shouldSucceed_whenRedisReturnsSuccess() {
            // given
            val coupon = mockCoupon()
            every { couponRepository.issue(coupon, customerId) } returns CouponIssueResult.SUCCESS

            // when & then
            assertThatCode {
                couponService.issue(coupon, customerId)
            }.doesNotThrowAnyException()
        }

        @Test
        @DisplayName("쿠폰 정보가 존재하지 않을 경우 예외 발생")
        fun shouldThrow_whenCouponNotExist() {
            // given
            val coupon = mockCoupon()
            every { couponRepository.issue(coupon, customerId) } returns CouponIssueResult.NON_FOUND

            // when & then
            assertThatThrownBy {
                couponService.issue(coupon, customerId)
            }.isInstanceOf(CouponNotFoundException::class.java)
        }

        @Test
        @DisplayName("이미 발급된 쿠폰일 경우 예외 발생")
        fun shouldThrow_whenAlreadyIssued() {
            // given
            val coupon = mockCoupon()
            every { couponRepository.issue(coupon, customerId) } returns CouponIssueResult.ALREADY_ISSUED

            // when & then
            assertThatThrownBy {
                couponService.issue(coupon, customerId)
            }.isInstanceOf(CustomerCouponAlreadyIssuedException::class.java)
        }

        @Test
        @DisplayName("쿠폰 수량 부족 시 예외 발생")
        fun shouldThrow_whenCouponInsufficient() {
            // given
            val coupon = mockCoupon()
            every { couponRepository.issue(coupon, customerId) } returns CouponIssueResult.INSUFFICIENT

            // when & then
            assertThatThrownBy {
                couponService.issue(coupon, customerId)
            }.isInstanceOf(CouponInsufficientException::class.java)
        }

        @Test
        @DisplayName("기타 예외 상황 발생 시 CouponIssueFailedException 발생")
        fun shouldThrow_whenUnknownErrorOccurs() {
            // given
            val coupon = mockCoupon()
            every { couponRepository.issue(coupon, customerId) } returns CouponIssueResult.UNKNOWN

            // when & then
            assertThatThrownBy {
                couponService.issue(coupon, customerId)
            }.isInstanceOf(CouponIssueFailedException::class.java)
        }
    }
}

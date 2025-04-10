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
    private val customerCouponRepository = mockk<CustomerCouponRepository>()
    private val couponService = CouponService(couponRepository, customerCouponRepository)

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
    inner class ValidateAndCalculateDiscount {
        private val couponId = 1L
        private val customerId = 10L

        @Test
        @DisplayName("정액 할인 쿠폰일 경우 지정된 할인 금액 반환")
        fun returnFixedDiscountAmount_whenCouponIsFixed() {
            // given
            val now = LocalDateTime.now()
            val customer = Customer("tester")
            val coupon = Coupon(
                name = "5천원 할인",
                discountType = DiscountType.FIXED,
                discountAmount = 5000,
                currentQuantity = 50,
                totalQuantity = 100,
                startedAt = now.minusDays(1),
                expiredAt = now.plusDays(1)
            ).apply { id = couponId }

            val customerCoupon = CustomerCoupon(customer, coupon)
            every { customerCouponRepository.findByCustomerIdAndCouponId(customerId, couponId) } returns customerCoupon
            every { couponRepository.findById(couponId) } returns coupon

            // when
            val result = couponService.validateAndCalculateDiscount(couponId, customerId, totalPrice = 100_000)

            // then
            assertThat(result).isEqualTo(5000)
        }

        @Test
        @DisplayName("퍼센트 할인 쿠폰일 경우 할인 금액 계산하여 반환")
        fun returnRateDiscountAmount_whenCouponIsRate() {
            // given
            val now = LocalDateTime.now()
            val customer = Customer("tester")
            val coupon = Coupon(
                name = "10% 할인",
                discountType = DiscountType.RATE,
                discountAmount = 10,
                currentQuantity = 20,
                totalQuantity = 100,
                startedAt = now.minusDays(1),
                expiredAt = now.plusDays(1)
            ).apply { id = couponId }

            val customerCoupon = CustomerCoupon(customer, coupon)
            every { customerCouponRepository.findByCustomerIdAndCouponId(customerId, couponId) } returns customerCoupon
            every { couponRepository.findById(couponId) } returns coupon

            // when
            val result = couponService.validateAndCalculateDiscount(couponId, customerId, totalPrice = 100_000)

            // then
            assertThat(result).isEqualTo(10_000)
        }

        @Test
        @DisplayName("고객에게 발급되지 않은 쿠폰일 경우 예외 발생")
        fun throwException_whenCouponNotIssuedToCustomer() {
            // given
            every { customerCouponRepository.findByCustomerIdAndCouponId(customerId, couponId) } returns null

            // when
            val exception = assertThrows<IllegalArgumentException> {
                couponService.validateAndCalculateDiscount(couponId, customerId, totalPrice = 100_000)
            }

            // then
            assertThat(exception).hasMessage("해당 쿠폰은 고객에게 발급되지 않았습니다.")
        }

        @Test
        @DisplayName("사용 불가 상태의 쿠폰일 경우 예외 발생")
        fun throwException_whenCouponIsUsedOrExpired() {
            // given
            val customer = Customer("tester")
            val coupon = Coupon(
                name = "만료쿠폰",
                discountType = DiscountType.FIXED,
                discountAmount = 5000,
                currentQuantity = 0,
                totalQuantity = 100,
                startedAt = LocalDateTime.now().minusDays(5),
                expiredAt = LocalDateTime.now().plusDays(5)
            ).apply { id = couponId }

            val customerCoupon = CustomerCoupon(customer, coupon).apply {
                status = CustomerCouponStatus.USED
            }

            every { customerCouponRepository.findByCustomerIdAndCouponId(customerId, couponId) } returns customerCoupon

            // when
            val exception = assertThrows<IllegalStateException> {
                couponService.validateAndCalculateDiscount(couponId, customerId, totalPrice = 100_000)
            }

            // then
            assertThat(exception).hasMessage("만료되었거나 사용된 쿠폰입니다.")
        }

        @Test
        @DisplayName("유효기간 밖의 쿠폰일 경우 예외 발생")
        fun throwException_whenCouponNotWithinPeriod() {
            // given
            val now = LocalDateTime.now()
            val customer = Customer("tester")
            val coupon = Coupon(
                name = "기간만료쿠폰",
                discountType = DiscountType.FIXED,
                discountAmount = 3000,
                currentQuantity = 20,
                totalQuantity = 100,
                startedAt = now.minusDays(10),
                expiredAt = now.minusDays(1)
            ).apply { id = couponId }

            val customerCoupon = CustomerCoupon(customer, coupon)
            every { customerCouponRepository.findByCustomerIdAndCouponId(customerId, couponId) } returns customerCoupon
            every { couponRepository.findById(couponId) } returns coupon

            // when
            val exception = assertThrows<IllegalStateException> {
                couponService.validateAndCalculateDiscount(couponId, customerId, totalPrice = 100_000)
            }

            // then
            assertThat(exception).hasMessage("유효하지 않은 쿠폰입니다.")
        }
    }
}

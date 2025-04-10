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

class CustomerCouponServiceTest {
    private val customerCouponRepository = mockk<CustomerCouponRepository>()
    private val customerCouponService = CustomerCouponService(customerCouponRepository)

    @Nested
    inner class ValidateIssuedCoupon {

        private val customerId = 1L
        private val couponId = 10L

        @Test
        @DisplayName("고객에게 발급된 사용 가능한 쿠폰이면 반환")
        fun returnCustomerCoupon_whenValidAndAvailable() {
            // given
            val customer = Customer("tester").apply { id = customerId }
            val coupon = Coupon(
                name = "정상 쿠폰",
                discountType = DiscountType.FIXED,
                discountAmount = 1000,
                currentQuantity = 10,
                totalQuantity = 100,
                startedAt = LocalDateTime.now().minusDays(1),
                expiredAt = LocalDateTime.now().plusDays(1)
            ).apply { id = couponId }

            val customerCoupon = CustomerCoupon(customer, coupon).apply {
                status = CustomerCouponStatus.AVAILABLE
            }

            every { customerCouponRepository.findByCustomerIdAndCouponId(customerId, couponId) } returns customerCoupon

            // when
            val result = customerCouponService.validateIssuedCoupon(customerId, couponId)

            // then
            assertThat(result).isEqualTo(customerCoupon)
        }

        @Test
        @DisplayName("고객에게 발급되지 않은 쿠폰이면 예외 발생")
        fun throwException_whenNotIssued() {
            // given
            every { customerCouponRepository.findByCustomerIdAndCouponId(customerId, couponId) } returns null

            // when
            val exception = assertThrows<IllegalArgumentException> {
                customerCouponService.validateIssuedCoupon(customerId, couponId)
            }

            // then
            assertThat(exception)
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("해당 쿠폰은 고객에게 발급되지 않았습니다.")
        }

        @Test
        @DisplayName("이미 사용된 쿠폰이면 예외 발생")
        fun throwException_whenUsed() {
            // given
            val customer = Customer("tester").apply { id = customerId }
            val coupon = Coupon(
                name = "사용된 쿠폰",
                discountType = DiscountType.FIXED,
                discountAmount = 1000,
                currentQuantity = 0,
                totalQuantity = 100,
                startedAt = LocalDateTime.now().minusDays(3),
                expiredAt = LocalDateTime.now().plusDays(2)
            ).apply { id = couponId }

            val customerCoupon = CustomerCoupon(customer, coupon).apply {
                status = CustomerCouponStatus.USED
            }

            every { customerCouponRepository.findByCustomerIdAndCouponId(customerId, couponId) } returns customerCoupon

            // when
            val exception = assertThrows<IllegalStateException> {
                customerCouponService.validateIssuedCoupon(customerId, couponId)
            }

            // then
            assertThat(exception)
                .hasMessage("이미 사용된 쿠폰입니다.")
        }

        @Test
        @DisplayName("만료된 쿠폰이면 예외 발생")
        fun throwException_whenExpired() {
            // given
            val customer = Customer("tester").apply { id = customerId }
            val coupon = Coupon(
                name = "만료 쿠폰",
                discountType = DiscountType.FIXED,
                discountAmount = 1000,
                currentQuantity = 0,
                totalQuantity = 100,
                startedAt = LocalDateTime.now().minusDays(10),
                expiredAt = LocalDateTime.now().minusDays(1)
            ).apply { id = couponId }

            val customerCoupon = CustomerCoupon(customer, coupon).apply {
                status = CustomerCouponStatus.EXPIRED
            }

            every { customerCouponRepository.findByCustomerIdAndCouponId(customerId, couponId) } returns customerCoupon

            // when
            val exception = assertThrows<IllegalStateException> {
                customerCouponService.validateIssuedCoupon(customerId, couponId)
            }

            // then
            assertThat(exception)
                .hasMessage("사용 기간이 만료된 쿠폰입니다.")
        }
    }
}

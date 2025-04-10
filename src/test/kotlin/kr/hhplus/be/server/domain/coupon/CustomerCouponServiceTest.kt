package kr.hhplus.be.server.domain.coupon

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.server.domain.customer.Customer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate

class CustomerCouponServiceTest {
    private val customerCouponRepository = mockk<CustomerCouponRepository>()
    private val customerCouponService = CustomerCouponService(customerCouponRepository)

    @Nested
    inner class GetAllByCustomerId {
        @Test
        @DisplayName("고객 ID에 해당하는 모든 쿠폰을 반환")
        fun returnAllCouponsForCustomer() {
            // given
            val customerId = 1L
            val customer = Customer("tester").apply { id = customerId }

            val coupon1 = Coupon(
                name = "5천원 할인",
                discountType = DiscountType.FIXED,
                discountAmount = 5000,
                currentQuantity = 100,
                totalQuantity = 100,
                startedAt = LocalDate.now().minusDays(1),
                expiredAt = LocalDate.now().plusDays(5)
            ).apply { id = 1L }

            val coupon2 = Coupon(
                name = "1만원 할인",
                discountType = DiscountType.FIXED,
                discountAmount = 10000,
                currentQuantity = 50,
                totalQuantity = 50,
                startedAt = LocalDate.now().minusDays(2),
                expiredAt = LocalDate.now().plusDays(3)
            ).apply { id = 2L }

            val expectedCoupons = listOf(
                CustomerCoupon(customer, coupon1).apply { id = 1L },
                CustomerCoupon(customer, coupon2).apply { id = 2L }
            )

            every { customerCouponRepository.findAllByCustomerId(customerId) } returns expectedCoupons

            // when
            val result = customerCouponService.getAllByCustomerId(customerId)

            // then
            assertThat(result).isEqualTo(expectedCoupons)
            verify(exactly = 1) { customerCouponRepository.findAllByCustomerId(customerId) }
        }
    }

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
                startedAt = LocalDate.now().minusDays(1),
                expiredAt = LocalDate.now().plusDays(1)
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
            every { customerCouponRepository.findByCustomerIdAndCouponId(customerId, couponId) } returns null

            val exception = assertThrows<IllegalArgumentException> {
                customerCouponService.validateIssuedCoupon(customerId, couponId)
            }

            assertThat(exception).hasMessage("해당 쿠폰은 고객에게 발급되지 않았습니다.")
        }

        @Test
        @DisplayName("이미 사용된 쿠폰이면 예외 발생")
        fun throwException_whenUsed() {
            val customer = Customer("tester").apply { id = customerId }
            val coupon = Coupon(
                name = "사용된 쿠폰",
                discountType = DiscountType.FIXED,
                discountAmount = 1000,
                currentQuantity = 0,
                totalQuantity = 100,
                startedAt = LocalDate.now().minusDays(3),
                expiredAt = LocalDate.now().plusDays(2)
            ).apply { id = couponId }
            val customerCoupon = CustomerCoupon(customer, coupon).apply {
                status = CustomerCouponStatus.USED
            }

            every { customerCouponRepository.findByCustomerIdAndCouponId(customerId, couponId) } returns customerCoupon

            val exception = assertThrows<IllegalStateException> {
                customerCouponService.validateIssuedCoupon(customerId, couponId)
            }

            assertThat(exception).hasMessage("이미 사용된 쿠폰입니다.")
        }

        @Test
        @DisplayName("만료된 쿠폰이면 예외 발생")
        fun throwException_whenExpired() {
            val customer = Customer("tester").apply { id = customerId }
            val coupon = Coupon(
                name = "만료 쿠폰",
                discountType = DiscountType.FIXED,
                discountAmount = 1000,
                currentQuantity = 0,
                totalQuantity = 100,
                startedAt = LocalDate.now().minusDays(10),
                expiredAt = LocalDate.now().minusDays(1)
            ).apply { id = couponId }
            val customerCoupon = CustomerCoupon(customer, coupon).apply {
                status = CustomerCouponStatus.EXPIRED
            }

            every { customerCouponRepository.findByCustomerIdAndCouponId(customerId, couponId) } returns customerCoupon

            val exception = assertThrows<IllegalStateException> {
                customerCouponService.validateIssuedCoupon(customerId, couponId)
            }

            assertThat(exception).hasMessage("사용 기간이 만료된 쿠폰입니다.")
        }
    }

    @Nested
    inner class ValidateNotIssued {
        private val customerId = 1L
        private val couponId = 1L

        @Test
        @DisplayName("쿠폰이 아직 발급되지 않은 경우 예외 없이 통과")
        fun doesNotThrow_whenCouponNotIssued() {
            // given
            every { customerCouponRepository.findByCustomerIdAndCouponId(customerId, couponId) } returns null

            // when & then
            customerCouponService.validateNotIssued(customerId, couponId)
        }

        @Test
        @DisplayName("쿠폰이 이미 발급된 경우 예외 발생")
        fun throwsException_whenCouponAlreadyIssued() {
            // given
            val customer = Customer("tester").apply { id = customerId }
            val coupon = Coupon(
                name = "할인쿠폰",
                discountType = DiscountType.FIXED,
                discountAmount = 3000,
                currentQuantity = 100,
                totalQuantity = 100,
                startedAt = LocalDate.now().minusDays(1),
                expiredAt = LocalDate.now().plusDays(1)
            ).apply { id = couponId }

            val issued = CustomerCoupon(customer, coupon)

            every { customerCouponRepository.findByCustomerIdAndCouponId(customerId, couponId) } returns issued

            // when
            val exception = assertThrows<IllegalStateException> {
                customerCouponService.validateNotIssued(customerId, couponId)
            }

            // then
            assertThat(exception)
                .hasMessage("해당 쿠폰은 이미 발급된 쿠폰입니다.")
        }
    }

    @Nested
    inner class Issue {
        @Test
        @DisplayName("쿠폰을 발급하고 저장된 객체 반환")
        fun saveAndReturnCustomerCoupon() {
            // given
            val customer = Customer("tester").apply { id = 1L }
            val coupon = Coupon(
                name = "웰컴쿠폰",
                discountType = DiscountType.FIXED,
                discountAmount = 1000,
                currentQuantity = 100,
                totalQuantity = 100,
                startedAt = LocalDate.now().minusDays(1),
                expiredAt = LocalDate.now().plusDays(1)
            ).apply { id = 2L }

            val saved = CustomerCoupon(customer, coupon).apply { id = 1L }

            every { customerCouponRepository.save(any()) } returns saved

            // when
            val result = customerCouponService.issue(customer, coupon)

            // then
            assertThat(result).isEqualTo(saved)
            verify(exactly = 1) { customerCouponRepository.save(any()) }
        }
    }
}

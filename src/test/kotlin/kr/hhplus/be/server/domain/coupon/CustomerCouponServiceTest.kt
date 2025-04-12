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
            val customer = Customer.create("tester").apply { id = customerId }

            val coupon1 = Coupon.createFixedDiscount(
                name = "5천원 할인",
                amount = 5000,
                quantity = 100,
                startedAt = LocalDate.now().minusDays(1),
                expiredAt = LocalDate.now().plusDays(5)
            ).apply { id = 1L }

            val coupon2 = Coupon.createFixedDiscount(
                name = "1만원 할인",
                amount = 10000,
                quantity = 50,
                startedAt = LocalDate.now().minusDays(2),
                expiredAt = LocalDate.now().plusDays(3)
            ).apply { id = 2L }

            val expectedCoupons = listOf(
                CustomerCoupon.issue(customer, coupon1).apply { id = 1L },
                CustomerCoupon.issue(customer, coupon2).apply { id = 2L }
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
            val customer = Customer.create("tester").apply { id = customerId }
            val coupon = Coupon.createFixedDiscount(
                name = "정상 쿠폰",
                amount = 1000,
                quantity = 100,
                startedAt = LocalDate.now().minusDays(1),
                expiredAt = LocalDate.now().plusDays(1)
            ).apply { id = couponId }
            val customerCoupon = CustomerCoupon.issue(customer, coupon).apply {
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
            val customer = Customer.create("tester").apply { id = customerId }
            val coupon = Coupon.createFixedDiscount(
                name = "사용된 쿠폰",
                amount = 1000,
                quantity = 100,
                startedAt = LocalDate.now().minusDays(3),
                expiredAt = LocalDate.now().plusDays(2)
            ).apply { id = couponId }
            val customerCoupon = CustomerCoupon.issue(customer, coupon).apply {
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
            val customer = Customer.create("tester").apply { id = customerId }
            val coupon = Coupon.createFixedDiscount(
                name = "만료 쿠폰",
                amount = 1000,
                quantity = 100,
                startedAt = LocalDate.now().minusDays(10),
                expiredAt = LocalDate.now().minusDays(1)
            ).apply { id = couponId }
            val customerCoupon = CustomerCoupon.issue(customer, coupon).apply {
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
            val customer = Customer.create("tester").apply { id = customerId }
            val coupon = Coupon.createFixedDiscount(
                name = "할인쿠폰",
                amount = 3000,
                quantity = 100,
                startedAt = LocalDate.now().minusDays(1),
                expiredAt = LocalDate.now().plusDays(1)
            ).apply { id = couponId }

            val issued = CustomerCoupon.issue(customer, coupon)

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
            val customer = Customer.create("tester").apply { id = 1L }
            val coupon = Coupon.createFixedDiscount(
                name = "웰컴쿠폰",
                amount = 1000,
                quantity = 100,
                startedAt = LocalDate.now().minusDays(1),
                expiredAt = LocalDate.now().plusDays(1)
            ).apply { id = 2L }

            val saved = CustomerCoupon.issue(customer, coupon).apply { id = 1L }

            every { customerCouponRepository.save(any()) } returns saved

            // when
            val result = customerCouponService.issue(customer, coupon)

            // then
            assertThat(result).isEqualTo(saved)
            verify(exactly = 1) { customerCouponRepository.save(any()) }
        }
    }

    @Nested
    @DisplayName("만료 쿠폰 상태 갱신")
    inner class UpdateAsExpired {
        @Test
        @DisplayName("AVAILABLE 상태인 쿠폰은 EXPIRED 로 갱신된다")
        fun shouldUpdateAvailableToExpired() {
            // given
            val coupon = Coupon.createFixedDiscount(
                name = "할인 가능 쿠폰",
                amount = 1000,
                quantity = 100,
                startedAt = LocalDate.now().minusDays(10),
                expiredAt = LocalDate.now().minusDays(1)
            ).apply { id = 1L }
            val customer = Customer.create("tester").apply { id = 1L }

            val available = CustomerCoupon.issue(customer, coupon).apply {
                id = 1L
                status = CustomerCouponStatus.AVAILABLE
            }

            every { customerCouponRepository.findAllByCouponIn(listOf(coupon)) } returns listOf(available)
            every { customerCouponRepository.saveAll(any()) } returnsArgument 0

            // when
            customerCouponService.updateAsExpired(listOf(coupon))

            // then
            assertThat(available.status).isEqualTo(CustomerCouponStatus.EXPIRED)
            verify { customerCouponRepository.saveAll(listOf(available)) }
        }

        @Test
        @DisplayName("USED 상태인 쿠폰은 변경되지 않는다")
        fun shouldNotUpdateUsedCoupon() {
            // given
            val coupon = Coupon.createFixedDiscount(
                name = "이전에 사용한 쿠폰",
                amount = 1000,
                quantity = 100,
                startedAt = LocalDate.now().minusDays(10),
                expiredAt = LocalDate.now().minusDays(1)
            ).apply { id = 2L }
            val customer = Customer.create("tester").apply { id = 2L }

            val used = CustomerCoupon.issue(customer, coupon).apply {
                id = 2L
                status = CustomerCouponStatus.USED
            }

            every { customerCouponRepository.findAllByCouponIn(listOf(coupon)) } returns listOf(used)
            every { customerCouponRepository.saveAll(any()) } returnsArgument 0

            // when
            customerCouponService.updateAsExpired(listOf(coupon))

            // then
            assertThat(used.status).isEqualTo(CustomerCouponStatus.USED)
            verify { customerCouponRepository.saveAll(listOf(used)) }
        }
    }
}

package kr.hhplus.be.server.domain.coupon

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.server.domain.customer.Customer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime

class CustomerCouponServiceTest {
    private val customerCouponRepository = mockk<CustomerCouponRepository>()
    private val customerCouponService = CustomerCouponService(customerCouponRepository)

    @Test
    @DisplayName("고객 ID와 쿠폰 ID로 발급된 쿠폰을 조회")
    fun returnCustomerCoupon_whenExists() {
        // given
        val customerId = 1L
        val couponId = 10L
        val customer = Customer("tester").apply { id = customerId }
        val coupon = Coupon(
            name = "10% 할인",
            discountType = DiscountType.FIXED,
            discountAmount = 1000,
            currentQuantity = 100,
            totalQuantity = 100,
            startedAt = LocalDateTime.now().minusDays(1),
            expiredAt = LocalDateTime.now().plusDays(1)
        ).apply { id = couponId }

        val customerCoupon = CustomerCoupon(
            customer,
            coupon
        ).apply { id = 100L }

        every { customerCouponRepository.findByCustomerIdAndCouponId(customerId, couponId) } returns customerCoupon

        // when
        val result = customerCouponService.getByCustomerIdAndCouponId(customerId, couponId)

        // then
        assertThat(result).isEqualTo(customerCoupon)
        verify(exactly = 1) {
            customerCouponRepository.findByCustomerIdAndCouponId(customerId, couponId)
        }
    }

    @Test
    @DisplayName("발급된 쿠폰이 없을 경우 예외 발생")
    fun throwException_whenCustomerCouponNotFound() {
        // given
        val customerId = 1L
        val couponId = 999L

        every { customerCouponRepository.findByCustomerIdAndCouponId(customerId, couponId) } returns null

        // when
        val exception = assertThrows<IllegalArgumentException> {
            customerCouponService.getByCustomerIdAndCouponId(customerId, couponId)
        }

        // then
        assertThat(exception)
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("해당 쿠폰은 고객에게 발급되지 않았습니다.")
    }
}

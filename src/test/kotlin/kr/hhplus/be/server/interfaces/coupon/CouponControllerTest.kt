package kr.hhplus.be.server.interfaces.coupon

import io.mockk.every
import io.mockk.mockk
import kr.hhplus.be.server.application.coupon.CouponFacade
import kr.hhplus.be.server.domain.coupon.*
import kr.hhplus.be.server.domain.customer.Customer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.time.LocalDateTime

class CouponControllerTest {
    private val couponFacade = mockk<CouponFacade>()
    private val customerCouponService = mockk<CustomerCouponService>()
    private val couponController = CouponController(couponFacade, customerCouponService)

    @Test
    @DisplayName("쿠폰 발급 요청 시 발급된 쿠폰 반환")
    fun issue_shouldReturnIssuedCouponResponse() {
        // given
        val request = CouponRequest.Issue(couponId = 1L, customerId = 1L)
        val customer = Customer("tester").apply { id = 1L }
        val coupon = Coupon(
            name = "첫 구매 할인",
            discountType = DiscountType.FIXED,
            discountAmount = 3000,
            currentQuantity = 100,
            totalQuantity = 100,
            startedAt = LocalDateTime.now().minusDays(1),
            expiredAt = LocalDateTime.now().plusDays(10)
        ).apply { id = 1L }

        val customerCoupon = CustomerCoupon(customer, coupon).apply {
            id = 10L
            status = CustomerCouponStatus.AVAILABLE
        }

        every { couponFacade.issueCouponToCustomer(1L, 1L) } returns customerCoupon

        // when
        val response = couponController.issue(request)

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
        assertThat(response.body).isEqualTo(CouponResponse.Issue.from(customerCoupon))
    }

    @Test
    @DisplayName("사용자 쿠폰 목록 조회 시 보유 쿠폰 리스트 반환")
    fun getCustomerCoupons_shouldReturnOwnedCoupons() {
        // given
        val customer = Customer("tester").apply { id = 1L }
        val coupon1 = Coupon(
            name = "첫 구매 할인",
            discountType = DiscountType.FIXED,
            discountAmount = 3000,
            currentQuantity = 10,
            totalQuantity = 100,
            startedAt = LocalDateTime.now().minusDays(5),
            expiredAt = LocalDateTime.now().plusDays(5)
        ).apply { id = 1L }
        val coupon2 = Coupon(
            name = "봄맞이 프로모션",
            discountType = DiscountType.RATE,
            discountAmount = 10,
            currentQuantity = 20,
            totalQuantity = 100,
            startedAt = LocalDateTime.now().minusDays(15),
            expiredAt = LocalDateTime.now().plusDays(3)
        ).apply { id = 2L }

        val customerCoupons = listOf(
            CustomerCoupon(customer, coupon1).apply {
                id = 1L
                status = CustomerCouponStatus.AVAILABLE
            },
            CustomerCoupon(customer, coupon2).apply {
                id = 2L
                status = CustomerCouponStatus.USED
            }
        )

        every { customerCouponService.getAllByCustomerId(1L) } returns customerCoupons

        // when
        val response = couponController.getCustomerCoupons(1L)

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isEqualTo(customerCoupons.map { CouponResponse.Owned.from(it) })
    }
}

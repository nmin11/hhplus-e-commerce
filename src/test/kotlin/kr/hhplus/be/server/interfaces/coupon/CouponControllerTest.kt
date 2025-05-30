package kr.hhplus.be.server.interfaces.coupon

import io.mockk.every
import io.mockk.mockk
import kr.hhplus.be.server.application.coupon.CouponCommand
import kr.hhplus.be.server.application.coupon.CouponFacade
import kr.hhplus.be.server.application.coupon.CouponResult
import kr.hhplus.be.server.application.coupon.CustomerCouponResult
import kr.hhplus.be.server.domain.coupon.*
import kr.hhplus.be.server.domain.customer.Customer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.time.LocalDate

class CouponControllerTest {
    private val couponFacade = mockk<CouponFacade>()
    private val customerCouponService = mockk<CustomerCouponService>()
    private val couponController = CouponController(couponFacade, customerCouponService)

    @Test
    @DisplayName("쿠폰 발급 요청 시 발급된 쿠폰 반환")
    fun issue_shouldReturnIssuedCouponResponse() {
        // given
        val request = CouponRequest.Issue(couponId = 1L, customerId = 1L)
        val customer = Customer.create("tester")
        val coupon = Coupon.createFixedDiscount(
            name = "첫 구매 할인",
            amount = 3000,
            quantity = 100,
            startedAt = LocalDate.now().minusDays(1),
            expiredAt = LocalDate.now().plusDays(10)
        )

        val customerCoupon = CustomerCoupon.issue(customer, coupon).apply {
            status = CustomerCouponStatus.AVAILABLE
        }

        val command = CouponCommand.Issue(1L, 1L)
        val result = CouponResult.Issue.from(customerCoupon)
        every { couponFacade.issueCouponToCustomer(command) } returns result

        // when
        val response = couponController.issue(request)

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
        assertThat(response.body).isEqualTo(CouponResponse.Issue.from(result))
    }

    @Test
    @DisplayName("사용자 쿠폰 목록 조회 시 보유 쿠폰 리스트 반환")
    fun getCustomerCoupons_shouldReturnOwnedCoupons() {
        // given
        val customer = Customer.create("tester")
        val coupon1 = Coupon.createFixedDiscount(
            name = "첫 구매 할인",
            amount = 3000,
            quantity = 100,
            startedAt = LocalDate.now().minusDays(5),
            expiredAt = LocalDate.now().plusDays(5)
        )
        val coupon2 = Coupon.createRateDiscount(
            name = "봄맞이 프로모션",
            rate = 10,
            quantity = 100,
            startedAt = LocalDate.now().minusDays(15),
            expiredAt = LocalDate.now().plusDays(3)
        )

        val customerCoupons = listOf(
            CustomerCoupon.issue(customer, coupon1).apply {
                status = CustomerCouponStatus.AVAILABLE
            },
            CustomerCoupon.issue(customer, coupon2).apply {
                status = CustomerCouponStatus.USED
            }
        )

        val result = customerCoupons.map { CustomerCouponResult.from(it) }

        every { customerCouponService.getAllByCustomerId(1L) } returns customerCoupons

        // when
        val response = couponController.getCustomerCoupons(1L)

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isEqualTo(result.map { CouponResponse.Owned.from(it) })
    }
}

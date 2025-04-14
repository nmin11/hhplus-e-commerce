package kr.hhplus.be.server.interfaces.coupon

import io.mockk.every
import io.mockk.mockk
import kr.hhplus.be.server.application.coupon.CouponCommand
import kr.hhplus.be.server.application.coupon.CouponFacade
import kr.hhplus.be.server.application.coupon.CouponResult
import kr.hhplus.be.server.domain.coupon.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.time.LocalDate

class CouponControllerTest {
    private val couponFacade = mockk<CouponFacade>()
    private val couponController = CouponController(couponFacade)

    @Test
    @DisplayName("쿠폰 발급 요청 시 발급된 쿠폰 반환")
    fun issue_shouldReturnIssuedCouponResponse() {
        // given
        val couponId = 10L
        val customerId = 1L
        val request = CouponRequest.Issue(couponId, customerId)

        val customerCoupon = CustomerCoupon.issue(customerId, couponId).apply {
            status = CustomerCouponStatus.AVAILABLE
        }

        val command = CouponCommand.Issue(couponId, customerId)
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
        val firstCouponId = 11L
        val secondCouponId = 12L
        val customerId = 1L
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
            CustomerCoupon.issue(customerId, firstCouponId).apply {
                status = CustomerCouponStatus.AVAILABLE
            },
            CustomerCoupon.issue(customerId, secondCouponId).apply {
                status = CustomerCouponStatus.USED
            }
        )

        val result = mutableListOf<CouponResult.OwnedCoupon>()
        customerCoupons.map {
            result.add(CouponResult.OwnedCoupon.from(it, coupon1))
            result.add(CouponResult.OwnedCoupon.from(it, coupon2))
        }

        every { couponFacade.getCustomerCoupons(1L) } returns result

        // when
        val response = couponController.getCustomerCoupons(1L)

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isEqualTo(result.map { CouponResponse.Owned.from(it) })
    }
}

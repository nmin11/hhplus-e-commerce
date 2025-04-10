package kr.hhplus.be.server.interfaces.coupon

import kr.hhplus.be.server.application.coupon.CouponFacade
import kr.hhplus.be.server.domain.coupon.CustomerCouponService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class CouponController(
    private val couponFacade: CouponFacade,
    private val customerCouponService: CustomerCouponService
) : CouponApi {
    override fun issue(@RequestBody request: CouponRequest.Issue): ResponseEntity<CouponResponse.Issue> {
        val result = couponFacade.issueCouponToCustomer(request.couponId, request.customerId)
        val response = CouponResponse.Issue.from(result)
        return ResponseEntity.status(201).body(response)
    }

    override fun getCustomerCoupons(customerId: Long): ResponseEntity<List<CouponResponse.Owned>> {
        val results = customerCouponService.getAllByCustomerId(customerId)
        val response = results.map { CouponResponse.Owned.from(it) }
        return ResponseEntity.ok(response)
    }
}

package kr.hhplus.be.server.interfaces.coupon

import kr.hhplus.be.server.application.coupon.CouponCommand
import kr.hhplus.be.server.application.coupon.CouponFacade
import kr.hhplus.be.server.application.coupon.CustomerCouponResult
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
        val command = CouponCommand.Issue.from(request)
        val result = couponFacade.issueCouponToCustomer(command)
        val response = CouponResponse.Issue.from(result)
        return ResponseEntity.status(201).body(response)
    }

    override fun getCustomerCoupons(customerId: Long): ResponseEntity<List<CouponResponse.Owned>> {
        val customerCoupons = customerCouponService.getAllByCustomerId(customerId)
        val result = customerCoupons.map { CustomerCouponResult.from(it) }
        val response = result.map { CouponResponse.Owned.from(it) }
        return ResponseEntity.ok(response)
    }
}

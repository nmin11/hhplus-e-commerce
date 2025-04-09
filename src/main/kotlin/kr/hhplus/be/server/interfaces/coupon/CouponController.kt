package kr.hhplus.be.server.interfaces.coupon

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
class CouponController : CouponApi {
    override fun issue(id: Long, @RequestBody request: CouponRequest.Issue): ResponseEntity<CouponResponse.Issue> {
        val response = CouponResponse.Issue(
            couponId = id,
            customerId = request.customerId,
            status = "ISSUED",
            issuedAt = Instant.now().toString()
        )

        return ResponseEntity.status(201).body(response)
    }

    override fun getCustomerCoupons(customerId: Long): ResponseEntity<List<CouponResponse.Owned>> {
        val response = listOf(
            CouponResponse.Owned(
                name = "첫 구매 할인",
                discountType = "FIXED",
                discountAmount = 3000,
                status = "ISSUED",
                issuedAt = "2025-04-02T15:00:00Z",
                expiredAt = "2025-04-30T23:59:59Z"
            ),
            CouponResponse.Owned(
                name = "봄맞이 프로모션",
                discountType = "PERCENT",
                discountAmount = 10,
                status =  "USED",
                issuedAt = "2025-03-20T11:30:00Z",
                expiredAt = "2025-04-10T23:59:59Z"
            )
        )

        return ResponseEntity.ok(response)
    }
}

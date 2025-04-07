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
}

package kr.hhplus.be.server.controller

import kr.hhplus.be.server.api.CouponApi
import kr.hhplus.be.server.dto.CouponIssueRequest
import kr.hhplus.be.server.dto.CouponIssueResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
class CouponController : CouponApi {
    override fun issue(id: Long, @RequestBody request: CouponIssueRequest): ResponseEntity<CouponIssueResponse> {
        val response = CouponIssueResponse(
            couponId = id,
            customerId = request.customerId,
            status = "ISSUED",
            issuedAt = Instant.now().toString()
        )

        return ResponseEntity.status(201).body(response)
    }
}

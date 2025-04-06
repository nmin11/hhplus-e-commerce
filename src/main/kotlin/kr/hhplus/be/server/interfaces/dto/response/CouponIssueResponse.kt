package kr.hhplus.be.server.interfaces.dto.response

data class CouponIssueResponse(
    val couponId: Long,
    val customerId: Long,
    val status: String,
    val issuedAt: String
)
package kr.hhplus.be.server.dto

data class CouponIssueResponse(
    val couponId: Long,
    val customerId: Long,
    val status: String,
    val issuedAt: String
)

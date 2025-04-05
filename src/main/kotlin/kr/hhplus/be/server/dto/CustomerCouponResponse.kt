package kr.hhplus.be.server.dto

data class CustomerCouponResponse(
    val name: String,
    val discountType: String,
    val discountAmount: Int,
    val status: String,
    val issuedAt: String,
    val expiredAt: String
)

package kr.hhplus.be.server.interfaces.dto.response

data class CustomerCouponResponse(
    val name: String,
    val discountType: String,
    val discountAmount: Int,
    val status: String,
    val issuedAt: String,
    val expiredAt: String
)
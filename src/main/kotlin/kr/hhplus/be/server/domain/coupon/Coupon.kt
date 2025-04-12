package kr.hhplus.be.server.domain.coupon

import java.time.LocalDateTime

class Coupon(
    var name: String,
    var discountType: DiscountType,
    var discountAmount: Int,
    var currentQuantity: Int,
    var totalQuantity: Int,
    var startedAt: LocalDateTime,
    var expiredAt: LocalDateTime
) {
    var id: Long? = null
    val createdAt: LocalDateTime = LocalDateTime.now()
    var updatedAt: LocalDateTime = LocalDateTime.now()
    var customerCoupons: MutableList<CustomerCoupon> = mutableListOf()

    fun requireSavedId(): Long =
        id ?: throw IllegalStateException("Coupon 객체가 저장되지 않았습니다.")
}

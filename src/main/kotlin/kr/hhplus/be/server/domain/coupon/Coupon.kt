package kr.hhplus.be.server.domain.coupon

import java.time.LocalDate
import java.time.LocalDateTime

class Coupon private constructor(
    val name: String,
    val discountType: DiscountType,
    val discountAmount: Int,
    var currentQuantity: Int,
    val totalQuantity: Int,
    val startedAt: LocalDate,
    val expiredAt: LocalDate
) {
    var id: Long? = null
    val createdAt: LocalDateTime = LocalDateTime.now()
    var updatedAt: LocalDateTime = LocalDateTime.now()
    val customerCoupons: MutableList<CustomerCoupon> = mutableListOf()

    companion object {
        fun createFixedDiscount(
            name: String,
            discountAmount: Int,
            quantity: Int,
            startedAt: LocalDate,
            expiredAt: LocalDate
        ): Coupon {
            return Coupon(
                name = name,
                discountType = DiscountType.FIXED,
                discountAmount = discountAmount,
                currentQuantity = quantity,
                totalQuantity = quantity,
                startedAt = startedAt,
                expiredAt = expiredAt
            )
        }

        fun createRateDiscount(
            name: String,
            discountRate: Int,
            quantity: Int,
            startedAt: LocalDate,
            expiredAt: LocalDate
        ): Coupon {
            return Coupon(
                name = name,
                discountType = DiscountType.RATE,
                discountAmount = discountRate,
                currentQuantity = quantity,
                totalQuantity = quantity,
                startedAt = startedAt,
                expiredAt = expiredAt
            )
        }
    }

    fun requireSavedId(): Long =
        id ?: throw IllegalStateException("Coupon 객체가 저장되지 않았습니다.")
}

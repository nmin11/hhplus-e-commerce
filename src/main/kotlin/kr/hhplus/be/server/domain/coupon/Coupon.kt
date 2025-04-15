package kr.hhplus.be.server.domain.coupon

import jakarta.persistence.Transient
import java.time.LocalDate
import java.time.LocalDateTime

class Coupon private constructor(
    val name: String,
    val totalQuantity: Int,
    val startedAt: LocalDate,
    val expiredAt: LocalDate,
    val discountType: DiscountType,
    val discountAmount: Int
) {
    val id: Long = 0L
    private var currentQuantity: Int = totalQuantity
    val createdAt: LocalDateTime = LocalDateTime.now()
    private var updatedAt: LocalDateTime = LocalDateTime.now()

    @Transient
    private val discountPolicy: DiscountPolicy  =
        when (discountType) {
            DiscountType.FIXED -> FixedDiscountPolicy(discountAmount)
            DiscountType.RATE -> RateDiscountPolicy(discountAmount)
        }

    companion object {
        fun createFixedDiscount(
            name: String,
            amount: Int,
            quantity: Int,
            startedAt: LocalDate,
            expiredAt: LocalDate
        ): Coupon {
            return Coupon(
                name = name,
                totalQuantity = quantity,
                startedAt = startedAt,
                expiredAt = expiredAt,
                discountType = DiscountType.FIXED,
                discountAmount = amount
            )
        }

        fun createRateDiscount(
            name: String,
            rate: Int,
            quantity: Int,
            startedAt: LocalDate,
            expiredAt: LocalDate
        ): Coupon {
            return Coupon(
                name = name,
                totalQuantity = quantity,
                startedAt = startedAt,
                expiredAt = expiredAt,
                discountType = DiscountType.RATE,
                discountAmount = rate
            )
        }
    }

    fun decreaseQuantity() {
        if (currentQuantity <= 0) {
            throw IllegalStateException("쿠폰 수량이 모두 소진되었습니다.")
        }

        currentQuantity -= 1
        updatedAt = LocalDateTime.now()
    }

    fun calculateDiscount(totalPrice: Int): Int {
        validatePeriod()
        return discountPolicy.calculateDiscount(totalPrice)
    }

    fun validatePeriod(now: LocalDate = LocalDate.now()) {
        if (now.isBefore(startedAt) || now.isAfter(expiredAt)) {
            throw IllegalStateException("유효하지 않은 쿠폰입니다.")
        }
    }
}

package kr.hhplus.be.server.domain.coupon

import java.time.LocalDate
import java.time.LocalDateTime

class Coupon private constructor(
    val name: String,
    val quantity: Int,
    val startedAt: LocalDate,
    val expiredAt: LocalDate,
    val discountPolicy: DiscountPolicy
) {
    val id: Long = 0L
    private var currentQuantity: Int = quantity
    val totalQuantity: Int = quantity
    val createdAt: LocalDateTime = LocalDateTime.now()
    private var updatedAt: LocalDateTime = LocalDateTime.now()

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
                quantity = quantity,
                startedAt = startedAt,
                expiredAt = expiredAt,
                discountPolicy = FixedDiscountPolicy(amount)
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
                quantity = quantity,
                startedAt = startedAt,
                expiredAt = expiredAt,
                discountPolicy = RateDiscountPolicy(rate)
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
        val now = LocalDate.now()
        if (now.isBefore(startedAt) || now.isAfter(expiredAt)) {
            throw IllegalStateException("유효하지 않은 쿠폰입니다.")
        }
        return discountPolicy.calculateDiscount(totalPrice)
    }

    fun validatePeriod(now: LocalDate = LocalDate.now()) {
        if (now.isBefore(startedAt) || now.isAfter(expiredAt)) {
            throw IllegalStateException("유효하지 않은 쿠폰입니다.")
        }
    }
}

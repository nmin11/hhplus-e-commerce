package kr.hhplus.be.server.domain.product

import java.time.LocalDateTime

class Statistic private constructor(
    val product: Product,
    val salesCount: Int
) {
    var id: Long? = null
    var soldAt: LocalDateTime = LocalDateTime.now()

    companion object {
        fun create(product: Product, salesCount: Int): Statistic {
            require(salesCount > 0) { "판매 수량은 0보다 커야 합니다." }

            return Statistic(product, salesCount)
        }
    }

    fun requireSavedId(): Long =
        id ?: throw IllegalStateException("Statistic 객체가 저장되지 않았습니다.")
}

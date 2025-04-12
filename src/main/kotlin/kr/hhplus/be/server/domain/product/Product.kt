package kr.hhplus.be.server.domain.product

import java.time.LocalDateTime

class Product private constructor(
    val name: String,
    val basePrice: Int
) {
    var id: Long? = null
    val createdAt: LocalDateTime = LocalDateTime.now()
    val updatedAt: LocalDateTime = LocalDateTime.now()
    val productOptions: MutableList<ProductOption> = mutableListOf()
    val statistics: MutableList<Statistic> = mutableListOf()

    companion object {
        fun create(name: String, basePrice: Int): Product {
            require(name.isNotBlank()) { "상품 이름은 공백일 수 없습니다." }
            require(basePrice >= 0) { "기본 가격은 0 이상이어야 합니다." }
            return Product(name, basePrice)
        }
    }

    fun requireSavedId(): Long =
        id ?: throw IllegalStateException("Product 객체가 저장되지 않았습니다.")
}

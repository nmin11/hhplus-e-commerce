package kr.hhplus.be.server.domain.product

import jakarta.persistence.*
import kr.hhplus.be.server.domain.common.BaseEntity

@Entity
@Table(
    name = "product",
    uniqueConstraints = [UniqueConstraint(name = "uq_product_name", columnNames = ["name"])]
)
class Product private constructor(
    @Column(name = "name", nullable = false, length = 50)
    val name: String,

    @Column(name = "base_price", nullable = false)
    val basePrice: Int
) : BaseEntity() {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L

    companion object {
        fun create(name: String, basePrice: Int): Product {
            require(name.isNotBlank()) { "상품 이름은 공백일 수 없습니다." }
            require(basePrice >= 0) { "기본 가격은 0 이상이어야 합니다." }
            return Product(name, basePrice)
        }
    }
}

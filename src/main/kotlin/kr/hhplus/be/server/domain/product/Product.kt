package kr.hhplus.be.server.domain.product

import jakarta.persistence.*
import kr.hhplus.be.server.domain.common.BaseEntity
import kr.hhplus.be.server.support.exception.product.ProductInvalidBasePriceException
import kr.hhplus.be.server.support.exception.product.ProductNameBlankException

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
            if (name.isBlank()) throw ProductNameBlankException()
            if (basePrice < 0) throw ProductInvalidBasePriceException()
            return Product(name, basePrice)
        }
    }
}

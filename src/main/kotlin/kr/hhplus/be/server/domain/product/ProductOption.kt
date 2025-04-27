package kr.hhplus.be.server.domain.product

import jakarta.persistence.*
import kr.hhplus.be.server.domain.common.BaseEntity
import kr.hhplus.be.server.support.exception.product.ProductOptionInvalidExtraPriceException
import kr.hhplus.be.server.support.exception.product.ProductOptionNameBlankException

@Entity
@Table(
    name = "product_option",
    uniqueConstraints = [
        UniqueConstraint(name = "uq_product_option", columnNames = ["product_id", "option_name"])
    ]
)
class ProductOption private constructor(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    val product: Product,

    @Column(name = "option_name", nullable = false, length = 20)
    val optionName: String,

    @Column(name = "extra_price", nullable = false)
    val extraPrice: Int
) : BaseEntity() {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L

    companion object {
        fun create(product: Product, optionName: String, extraPrice: Int): ProductOption {
            if (optionName.isBlank()) throw ProductOptionNameBlankException()
            if (extraPrice < 0) throw ProductOptionInvalidExtraPriceException()
            return ProductOption(product, optionName, extraPrice)
        }
    }
}

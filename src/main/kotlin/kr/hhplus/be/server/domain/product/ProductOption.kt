package kr.hhplus.be.server.domain.product

import jakarta.persistence.*
import kr.hhplus.be.server.domain.common.BaseEntity

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
            require(optionName.isNotBlank()) { "옵션 이름은 공백일 수 없습니다." }
            require(extraPrice >= 0) { "추가 가격은 0 이상이어야 합니다." }

            return ProductOption(product, optionName, extraPrice)
        }
    }
}

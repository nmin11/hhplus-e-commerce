package kr.hhplus.be.server.domain.product

import jakarta.persistence.*
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "stock")
class Stock private constructor(
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_option_id", nullable = false, unique = true)
    val productOption: ProductOption,

    @Column(name = "quantity", nullable = false)
    var quantity: Int
) {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    lateinit var updatedAt: LocalDateTime

    companion object {
        fun create(productOption: ProductOption, quantity: Int): Stock {
            require(quantity >= 0) { "재고 수량은 0 이상이어야 합니다." }

            return Stock(productOption, quantity)
        }
    }

    fun decrease(quantityToDecrease: Int) {
        if (quantity < quantityToDecrease) {
            throw IllegalStateException("재고가 부족합니다.")
        }

        quantity -= quantityToDecrease
    }
}

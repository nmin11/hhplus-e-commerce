package kr.hhplus.be.server.domain.product

import jakarta.persistence.*
import kr.hhplus.be.server.support.exception.product.StockInsufficientException
import kr.hhplus.be.server.support.exception.product.StockInvalidQuantityException
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
            if (quantity < 0) throw StockInvalidQuantityException()

            return Stock(productOption, quantity)
        }
    }

    fun decrease(quantityToDecrease: Int) {
        if (quantity < quantityToDecrease) throw StockInsufficientException()

        quantity -= quantityToDecrease
    }

    fun increase(quantityToIncrease: Int) {
        quantity += quantityToIncrease
    }
}

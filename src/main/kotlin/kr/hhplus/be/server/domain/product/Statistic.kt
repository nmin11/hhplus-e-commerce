package kr.hhplus.be.server.domain.product

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "statistic")
class Statistic private constructor(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    val product: Product,

    @Column(name = "sales_count", nullable = false)
    val salesCount: Int
) {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L

    @Column(name = "sold_at", nullable = false)
    val soldAt: LocalDateTime = LocalDateTime.now()

    companion object {
        fun create(product: Product, salesCount: Int): Statistic {
            require(salesCount > 0) { "판매 수량은 0보다 커야 합니다." }

            return Statistic(product, salesCount)
        }
    }
}

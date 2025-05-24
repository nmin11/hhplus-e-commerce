package kr.hhplus.be.server.domain.balance

import jakarta.persistence.*
import kr.hhplus.be.server.domain.customer.Customer
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "balance_history")
class BalanceHistory private constructor(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    val customer: Customer,

    @Enumerated(EnumType.STRING)
    @Column(name = "change_type", nullable = false, length = 20)
    val changeType: BalanceChangeType,

    @Column(name = "change_amount", nullable = false)
    val changeAmount: Int,

    @Column(name = "total_amount", nullable = false)
    val totalAmount: Int
) {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    lateinit var createdAt: LocalDateTime

    companion object {
        fun charge(customer: Customer, amount: Int, updatedAmount: Int): BalanceHistory {
            return BalanceHistory(
                customer = customer,
                changeType = BalanceChangeType.CHARGE,
                changeAmount = amount,
                totalAmount = updatedAmount
            )
        }

        fun use(customer: Customer, amount: Int, updatedAmount: Int): BalanceHistory {
            return BalanceHistory(
                customer = customer,
                changeType = BalanceChangeType.USE,
                changeAmount = amount,
                totalAmount = updatedAmount
            )
        }

        fun rollback(customer: Customer, amount: Int, updatedAmount: Int): BalanceHistory {
            return BalanceHistory(
                customer = customer,
                changeType = BalanceChangeType.ROLLBACK,
                changeAmount = amount,
                totalAmount = updatedAmount
            )
        }
    }
}

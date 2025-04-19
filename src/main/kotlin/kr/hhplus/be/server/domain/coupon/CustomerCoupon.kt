package kr.hhplus.be.server.domain.coupon

import jakarta.persistence.*
import kr.hhplus.be.server.domain.customer.Customer
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

@Entity
@Table(
    name = "customer_coupon",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uq_customer_coupon_customer_id_coupon_id",
            columnNames = ["customer_id", "coupon_id"]
        )
    ]
)
class CustomerCoupon private constructor(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    val customer: Customer,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id", nullable = false)
    val coupon: Coupon
) {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    var status = CustomerCouponStatus.AVAILABLE

    @Column(name = "issued_at", nullable = false, updatable = false)
    val issuedAt: LocalDateTime = LocalDateTime.now()

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    lateinit var updatedAt: LocalDateTime

    companion object {
        fun issue(customer: Customer, coupon: Coupon): CustomerCoupon {
            return CustomerCoupon(
                customer = customer,
                coupon = coupon
            )
        }
    }

    fun expireIfAvailable() {
        if (status == CustomerCouponStatus.AVAILABLE) {
            status = CustomerCouponStatus.EXPIRED
            updatedAt = LocalDateTime.now()
        }
    }

    fun validateUsable(): CustomerCoupon {
        if (status == CustomerCouponStatus.USED) {
            throw IllegalStateException("이미 사용된 쿠폰입니다.")
        }
        if (status == CustomerCouponStatus.EXPIRED) {
            throw IllegalStateException("사용 기간이 만료된 쿠폰입니다.")
        }
        return this
    }
}

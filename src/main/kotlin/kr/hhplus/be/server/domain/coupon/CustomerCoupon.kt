package kr.hhplus.be.server.domain.coupon

import jakarta.persistence.*
import kr.hhplus.be.server.domain.customer.Customer
import kr.hhplus.be.server.support.exception.coupon.CustomerCouponAlreadyUsedException
import kr.hhplus.be.server.support.exception.coupon.CustomerCouponExpiredException
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

    @Version
    var version: Long = 0L

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
        }
    }

    fun markAsUsed() {
        checkValidation()
        status = CustomerCouponStatus.USED
    }

    fun validateUsable(): CustomerCoupon {
        checkValidation()
        return this
    }

    private fun checkValidation() {
        if (status == CustomerCouponStatus.USED) {
            throw CustomerCouponAlreadyUsedException()
        }
        if (status == CustomerCouponStatus.EXPIRED) {
            throw CustomerCouponExpiredException()
        }
    }
}

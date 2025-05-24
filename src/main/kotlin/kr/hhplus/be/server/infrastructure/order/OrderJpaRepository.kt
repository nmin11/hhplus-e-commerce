package kr.hhplus.be.server.infrastructure.order

import kr.hhplus.be.server.domain.order.Order
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface OrderJpaRepository : JpaRepository<Order, Long> {
    @Query("""
        SELECT o FROM Order o
        JOIN FETCH o.orderItems oi
        JOIN FETCH oi.productOption po
        JOIN FETCH po.product p
        WHERE o.id = :id
    """)
    fun findByIdWithDetails(id: Long): Order?
}

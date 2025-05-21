package kr.hhplus.be.server.domain.order

interface OrderRepository {
    fun save(order: Order): Order
    fun findById(id: Long): Order?
    fun findByIdWithDetails(id: Long): Order?
}

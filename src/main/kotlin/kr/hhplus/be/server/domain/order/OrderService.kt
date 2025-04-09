package kr.hhplus.be.server.domain.order

import org.springframework.stereotype.Service

@Service
class OrderService(
    private val orderRepository: OrderRepository
) {
    fun create(order: Order): Order {
        return orderRepository.save(order)
    }
}

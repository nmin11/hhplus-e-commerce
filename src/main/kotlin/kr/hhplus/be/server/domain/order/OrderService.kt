package kr.hhplus.be.server.domain.order

import org.springframework.stereotype.Service

@Service
class OrderService(
    private val orderRepository: OrderRepository
) {
    fun create(order: Order): Order {
        return orderRepository.save(order)
    }

    fun getById(id: Long): Order {
        return orderRepository.findById(id)
            ?: throw IllegalArgumentException("주문 정보를 찾을 수 없습니다.")
    }
}

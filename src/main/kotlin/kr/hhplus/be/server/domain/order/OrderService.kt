package kr.hhplus.be.server.domain.order

import jakarta.transaction.Transactional
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

    fun getValidOrderForPayment(orderId: Long): Order {
        val order = getById(orderId)
        if (order.status != OrderStatus.CREATED) {
            throw IllegalStateException("결제 가능한 주문이 아닙니다. (현재 상태: ${order.status})")
        }
        return order
    }

    @Transactional
    fun markAsPaid(order: Order) {
        order.markAsPaid()
    }
}

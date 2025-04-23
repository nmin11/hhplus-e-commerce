package kr.hhplus.be.server.domain.order

import jakarta.transaction.Transactional
import org.springframework.orm.ObjectOptimisticLockingFailureException
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
        try {
            order.markAsPaid()
            orderRepository.saveAndFlush(order)
        } catch (_: ObjectOptimisticLockingFailureException) {
            throw IllegalStateException("지금은 결제를 진행할 수 없습니다. 잠시 후 다시 시도해주세요.")
        }
    }
}

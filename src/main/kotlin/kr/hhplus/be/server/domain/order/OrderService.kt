package kr.hhplus.be.server.domain.order

import kr.hhplus.be.server.support.exception.order.OrderNotFoundException
import kr.hhplus.be.server.support.exception.order.OrderNotPayableException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OrderService(
    private val orderRepository: OrderRepository
) {
    @Transactional
    fun create(order: Order): Order {
        return orderRepository.save(order)
    }

    fun getById(id: Long): Order {
        return orderRepository.findById(id)
            ?: throw OrderNotFoundException()
    }

    fun getValidOrderForPayment(orderId: Long): Order {
        val order = getById(orderId)
        if (order.status != OrderStatus.CREATED) {
            throw OrderNotPayableException(order.status.name)
        }
        return order
    }

    @Transactional
    fun markAsPaid(order: Order) {
        order.markAsPaid()
        orderRepository.save(order)
    }
}

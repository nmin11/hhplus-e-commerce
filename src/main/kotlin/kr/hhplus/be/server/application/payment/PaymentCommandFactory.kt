package kr.hhplus.be.server.application.payment

import kr.hhplus.be.server.application.dataplatform.DataPlatformCommand
import kr.hhplus.be.server.domain.order.Order
import org.springframework.stereotype.Component

@Component
class PaymentCommandFactory {
    fun from(order: Order): DataPlatformCommand.Order {
        return DataPlatformCommand.Order(
            orderId = order.id,
            customerId = order.customer.id,
            totalPrice = order.totalPrice,
            createdAt = order.createdAt.toString(),
            items = order.orderItems.map {
                DataPlatformCommand.OrderItem(
                    productName = it.productOption.product.name,
                    optionName = it.productOption.optionName,
                    quantity = it.quantity,
                    subtotalPrice = it.subtotalPrice
                )
            }
        )
    }
}

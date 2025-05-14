package kr.hhplus.be.server.event

import kr.hhplus.be.server.domain.order.OrderItem
import org.springframework.stereotype.Component

@Component
class ProductEventFactory {
    fun from(orderItems: List<OrderItem>): ProductEvent.SalesUpdated {
        return ProductEvent.SalesUpdated(
            items = orderItems.map {
                ProductEvent.SoldItem(
                    productId = it.productOption.product.id,
                    quantity = it.quantity
                )
            }
        )
    }
}

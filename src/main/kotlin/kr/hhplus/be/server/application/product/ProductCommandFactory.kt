package kr.hhplus.be.server.application.product

import kr.hhplus.be.server.domain.order.OrderItem
import org.springframework.stereotype.Component

@Component
class ProductCommandFactory {
    fun from(orderItems: List<OrderItem>): ProductCommand.SalesUpdated {
        return ProductCommand.SalesUpdated(
            items = orderItems.map {
                ProductCommand.SoldItem(
                    productId = it.productOption.product.id,
                    quantity = it.quantity
                )
            }
        )
    }
}

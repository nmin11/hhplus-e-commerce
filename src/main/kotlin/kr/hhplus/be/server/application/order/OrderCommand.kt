package kr.hhplus.be.server.application.order

import kr.hhplus.be.server.interfaces.order.OrderRequest

sealed class OrderCommand {
    data class Create(
        val customerId: Long,
        val items: List<OrderItem>
    ) {
        companion object {
            fun from(request: OrderRequest.Create): Create {
                return Create(
                    customerId = request.customerId,
                    items = request.items.map {
                        OrderItem(
                            productId = it.productId,
                            productOptionId = it.productOptionId,
                            quantity = it.quantity
                        )
                    }
                )
            }
        }
    }

    data class OrderItem(
        val productId: Long,
        val productOptionId: Long,
        val quantity: Int
    )
}

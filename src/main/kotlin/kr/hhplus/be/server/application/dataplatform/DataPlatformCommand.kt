package kr.hhplus.be.server.application.dataplatform

import kr.hhplus.be.server.domain.payment.PaymentCompletedEvent

sealed class DataPlatformCommand {
    data class OrderPayload(
        val orderId: Long,
        val customerId: Long,
        val totalPrice: Int,
        val createdAt: String,
        val items: List<OrderPayloadItem>
    ) {
        companion object {
            fun from(event: PaymentCompletedEvent): OrderPayload {
                return OrderPayload(
                    orderId = event.orderId,
                    customerId = event.customerId,
                    totalPrice = event.totalPrice,
                    createdAt = event.createdAt.toString(),
                    items = event.items.map { OrderPayloadItem.from(it) }
                )
            }
        }
    }

    data class OrderPayloadItem(
        val productName: String,
        val optionName: String,
        val quantity: Int,
        val subtotalPrice: Int
    ) {
        companion object {
            fun from(item: PaymentCompletedEvent.Item): OrderPayloadItem {
                return OrderPayloadItem(
                    productName = item.productName,
                    optionName = item.optionName,
                    quantity = item.quantity,
                    subtotalPrice = item.subtotalPrice
                )
            }
        }
    }
}

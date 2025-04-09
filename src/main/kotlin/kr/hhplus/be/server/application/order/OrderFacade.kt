package kr.hhplus.be.server.application.order

import kr.hhplus.be.server.domain.customer.CustomerService
import kr.hhplus.be.server.domain.order.Order
import kr.hhplus.be.server.domain.order.OrderItem
import kr.hhplus.be.server.domain.order.OrderService
import kr.hhplus.be.server.domain.order.OrderStatus
import kr.hhplus.be.server.domain.product.ProductOptionService
import kr.hhplus.be.server.domain.product.ProductService
import kr.hhplus.be.server.domain.product.StockService
import kr.hhplus.be.server.interfaces.order.OrderRequest
import org.springframework.stereotype.Component

@Component
class OrderFacade(
    private val customerService: CustomerService,
    private val productService: ProductService,
    private val productOptionService: ProductOptionService,
    private val stockService: StockService,
    private val orderService: OrderService
) {

    fun createOrder(request: OrderRequest.Create): Order {
        val customer = customerService.getById(request.customerId)

        val order = Order(
            customer = customer,
            totalPrice = 0
        )

        val orderItems = request.items.map { item ->
            val product = productService.getById(item.productId)
            val option = productOptionService.getById(item.productOptionId)

            require(option.product.id == product.id) {
                "상품 ID와 옵션 ID가 일치하지 않습니다."
            }

            stockService.validate(option.id!!, item.quantity)

            val subtotal = (product.basePrice + option.extraPrice) * item.quantity

            OrderItem(
                order = order,
                productOption = option,
                quantity = item.quantity,
                subtotalPrice = subtotal
            )
        }

        order.orderItems.addAll(orderItems)
        order.totalPrice = orderItems.sumOf { it.subtotalPrice }

        return orderService.create(order)
    }
}

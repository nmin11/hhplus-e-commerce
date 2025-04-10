package kr.hhplus.be.server.application.order

import kr.hhplus.be.server.domain.customer.CustomerService
import kr.hhplus.be.server.domain.order.Order
import kr.hhplus.be.server.domain.order.OrderItem
import kr.hhplus.be.server.domain.order.OrderService
import kr.hhplus.be.server.domain.product.ProductOptionService
import kr.hhplus.be.server.domain.product.ProductService
import kr.hhplus.be.server.domain.product.StockService
import kr.hhplus.be.server.interfaces.order.OrderRequest
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class OrderFacade(
    private val customerService: CustomerService,
    private val productService: ProductService,
    private val productOptionService: ProductOptionService,
    private val stockService: StockService,
    private val orderService: OrderService
) {
    @Transactional
    fun createOrder(request: OrderRequest.Create): Order {
        // 1.사용자 조회
        val customer = customerService.getById(request.customerId)

        // 2. 객체 생성 작업
        val order = Order(
            customer = customer,
            totalPrice = 0
        )

        val orderItems = request.items.map { item ->
            val product = productService.getById(item.productId)
            val option = productOptionService.getById(item.productOptionId)

            // 2-1. 상품 옵션 및 재고 확인
            productOptionService.validateOptionBelongsToProduct(optionId = option.id, productId = product.id)
            stockService.validate(item.productOptionId, item.quantity)

            // 2-2. 항목별 주문액 계산
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

        // 3. 주문 생성
        return orderService.create(order)
    }
}

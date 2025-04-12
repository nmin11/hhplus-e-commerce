package kr.hhplus.be.server.application.order

import kr.hhplus.be.server.domain.customer.CustomerService
import kr.hhplus.be.server.domain.order.Order
import kr.hhplus.be.server.domain.order.OrderItemInfo
import kr.hhplus.be.server.domain.order.OrderService
import kr.hhplus.be.server.domain.product.ProductOption
import kr.hhplus.be.server.domain.product.ProductOptionService
import kr.hhplus.be.server.domain.product.ProductService
import kr.hhplus.be.server.domain.product.StockService
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
    fun createOrder(command: OrderCommand.Create): OrderResult.Create {
        // 1. 사용자 조회
        val customer = customerService.getById(command.customerId)

        // 2. 상품 옵션 목록 및 수량 확인
        val options: List<OrderItemInfo> = command.items.map { item ->
            val product = productService.getById(item.productId)
            val option = productOptionService.getById(item.productOptionId)

            productOptionService.validateOptionBelongsToProduct(option.id, product.id)
            stockService.validate(option.requireSavedId(), item.quantity)

            OrderItemInfo(option, item.quantity)
        }

        // 3. 주문 생성 및 저장
        val order = Order.createWithItems(customer, options)
        val savedOrder = orderService.create(order)

        return OrderResult.Create.from(savedOrder)
    }
}

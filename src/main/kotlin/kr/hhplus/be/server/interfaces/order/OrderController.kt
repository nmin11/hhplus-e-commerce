package kr.hhplus.be.server.interfaces.order

import kr.hhplus.be.server.application.order.OrderFacade
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class OrderController(
    private val orderFacade: OrderFacade
) : OrderApi {
    override fun create(@RequestBody request: OrderRequest.Create): ResponseEntity<OrderResponse.Create> {
//        val response = OrderResponse.Create(
//            orderId = 1,
//            customerId = request.customerId,
//            totalPrice = 87000,
//            createdAt = Instant.now().toString(),
//            items = listOf(
//                OrderResponse.OrderItem("청바지", "M", 1, 39000),
//                OrderResponse.OrderItem("후드티", "L", 1, 48000)
//            )
//        )

        val result = orderFacade.createOrder(request)
        val response = OrderResponse.Create.from(result)
        return ResponseEntity.status(201).body(response)
    }
}

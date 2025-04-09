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
        val result = orderFacade.createOrder(request)
        val response = OrderResponse.Create.from(result)
        return ResponseEntity.status(201).body(response)
    }
}

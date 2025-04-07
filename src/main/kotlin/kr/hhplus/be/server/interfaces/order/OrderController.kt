package kr.hhplus.be.server.interfaces.order

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
class OrderController : OrderApi {
    override fun create(@RequestBody request: OrderRequest.Create): ResponseEntity<OrderResponse.Create> {
        val response = OrderResponse.Create(
            orderId = 1,
            customerId = request.customerId,
            totalPrice = 87000,
            createdAt = Instant.now().toString(),
            items = listOf(
                OrderResponse.OrderItem("청바지", "M", 1, 39000),
                OrderResponse.OrderItem("후드티", "L", 1, 48000)
            )
        )

        return ResponseEntity.status(201).body(response)
    }
}

package kr.hhplus.be.server.controller

import kr.hhplus.be.server.api.OrderApi
import kr.hhplus.be.server.dto.OrderItemResponse
import kr.hhplus.be.server.dto.OrderRequest
import kr.hhplus.be.server.dto.OrderResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
class OrderController : OrderApi {
    override fun create(@RequestBody request: OrderRequest): ResponseEntity<OrderResponse> {
        val response = OrderResponse(
            orderId = 1,
            customerId = request.customerId,
            totalPrice = 87000,
            createdAt = Instant.now().toString(),
            items = listOf(
                OrderItemResponse("청바지", "M", 1, 39000),
                OrderItemResponse("후드티", "L", 1, 48000)
            )
        )

        return ResponseEntity.status(201).body(response)
    }
}

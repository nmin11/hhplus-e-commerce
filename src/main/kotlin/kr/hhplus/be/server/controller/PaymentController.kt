package kr.hhplus.be.server.controller

import kr.hhplus.be.server.api.PaymentApi
import kr.hhplus.be.server.dto.PaymentRequest
import kr.hhplus.be.server.dto.PaymentResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
class PaymentController : PaymentApi {
    override fun pay(@RequestBody request: PaymentRequest): ResponseEntity<PaymentResponse> {
        val response = PaymentResponse(
            paymentId = 1001,
            orderId = request.orderId,
            customerId = 1,
            originalPrice = 87000,
            discountAmount = 5000,
            discountedPrice = 82000,
            paidAt = Instant.now().toString()
        )

        return ResponseEntity.status(201).body(response)
    }
}

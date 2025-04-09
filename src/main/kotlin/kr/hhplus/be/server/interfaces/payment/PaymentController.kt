package kr.hhplus.be.server.interfaces.payment

import kr.hhplus.be.server.application.payment.PaymentFacade
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class PaymentController(
    private val paymentFacade: PaymentFacade
) : PaymentApi {
    override fun pay(@RequestBody request: PaymentRequest): ResponseEntity<PaymentResponse> {
        val result = paymentFacade.pay(request.orderId, request.couponId)
        val response = PaymentResponse.from(result)
        return ResponseEntity.status(201).body(response)
    }
}

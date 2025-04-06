package kr.hhplus.be.server.interfaces.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import kr.hhplus.be.server.interfaces.dto.request.PaymentRequest
import kr.hhplus.be.server.interfaces.dto.response.PaymentResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping

@Tag(name = "Payment", description = "결제 관련 API")
@RequestMapping("/payments")
interface PaymentApi {
    @Operation(summary = "결제")
    @ApiResponse(
        responseCode = "201",
        description = "결제 성공",
        content = [
            Content(
                mediaType = "application/json",
                schema = Schema(implementation = PaymentResponse::class),
                examples = [ExampleObject(
                    value = """
                    {
                      "paymentId": 1001,
                      "orderId": 2001,
                      "customerId": 1,
                      "originalPrice": 87000,
                      "discountAmount": 5000,
                      "discountedPrice": 82000,
                      "paidAt": "2025-04-02T14:00:00Z"
                    }
                    """
                )]
            )
        ]
    )
    @PostMapping
    fun pay(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "결제 요청 정보",
            required = true,
            content = [
                Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = PaymentRequest::class),
                    examples = [ExampleObject(
                        name = "기본 결제 요청",
                        summary = "쿠폰을 포함한 결제 요청",
                        value = """
                        {
                          "orderId": 1,
                          "couponId": 1
                        }
                        """
                    )]
                )
            ]
        )
        request: PaymentRequest
    ): ResponseEntity<PaymentResponse>
}

package kr.hhplus.be.server.interfaces.order

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping

@Tag(name = "Order", description = "주문 관련 API")
@RequestMapping("/orders")
interface OrderApi {

    @Operation(summary = "주문 생성")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "주문 생성 완료",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = OrderResponse.Create::class),
                        examples = [ExampleObject(
                            value = """
                    {
                      "orderId": 1,
                      "customerId": 1,
                      "totalPrice": 87000,
                      "createdAt": "2025-04-02T13:15:00Z",
                      "items": [
                        {
                          "productName": "청바지",
                          "optionName": "M",
                          "quantity": 1,
                          "subtotalPrice": 39000
                        },
                        {
                          "productName": "후드티",
                          "optionName": "L",
                          "quantity": 1,
                          "subtotalPrice": 48000
                        }
                      ]
                    }
                    """
                        )]
                    )
                ]
            ),
            ApiResponse(
                responseCode = "404",
                description = "사용자를 찾을 수 없음",
                content = [
                    Content(
                        mediaType = "application/json",
                        examples = [ExampleObject(value = """{"error":"사용자를 찾을 수 없습니다."}""")]
                    )
                ]
            ),
            ApiResponse(
                responseCode = "404",
                description = "쿠폰을 찾을 수 없음",
                content = [
                    Content(
                        mediaType = "application/json",
                        examples = [ExampleObject(value = """{"error":"쿠폰을 찾을 수 없습니다."}""")]
                    )
                ]
            )
        ]
    )
    @PostMapping
    fun create(
        @RequestBody(
            required = true,
            description = "주문 요청 정보",
            content = [
                Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = OrderRequest.Create::class),
                    examples = [ExampleObject(
                        name = "기본 주문 예시",
                        summary = "청바지 + 후드티 주문",
                        value = """
                        {
                          "customerId": 1,
                          "items": [
                            { "productId": 1, "productOptionId": 2, "quantity": 1 },
                            { "productId": 2, "productOptionId": 3, "quantity": 1 }
                          ]
                        }
                        """
                    )]
                )
            ]
        )
        request: OrderRequest.Create
    ): ResponseEntity<OrderResponse.Create>
}

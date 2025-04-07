package kr.hhplus.be.server.interfaces.customer

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import kr.hhplus.be.server.interfaces.balance.BalanceRequest
import kr.hhplus.be.server.interfaces.balance.BalanceResponse
import kr.hhplus.be.server.interfaces.coupon.CouponResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping

@Tag(name = "Customer", description = "사용자 관련 API")
@RequestMapping("/customers")
interface CustomerApi {
    @Operation(summary = "사용자 잔액 조회")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = [
                Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = BalanceResponse.Summary::class),
                    examples = [ExampleObject(value = """{"customerId":1,"amount":10000}""")]
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
        )
    )
    @GetMapping("/{id}/balance")
    fun getBalance(@PathVariable id: Long): ResponseEntity<BalanceResponse.Summary>

    @Operation(summary = "사용자 잔액 변경 내역 조회")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = BalanceResponse.Histories::class),
                        examples = [ExampleObject(
                            value = """
                            [
                              {
                                "changeType": "CHARGE",
                                "changeAmount": 10000,
                                "totalAmount": 60000,
                                "createdAt": "2025-04-02T16:31:11.959Z"
                              }
                            ]
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
            )
        ]
    )
    @GetMapping("/{id}/balance-histories")
    fun getBalanceHistories(@PathVariable id: Long): ResponseEntity<List<BalanceResponse.Histories>>

    @Operation(summary = "사용자 잔액 충전")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "충전 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = BalanceResponse::class),
                        examples = [ExampleObject(
                            value = """{"customerId":1,"amount":150000}"""
                        )]
                    )
                ]
            ),
            ApiResponse(
                responseCode = "400",
                description = "유효하지 않은 액수",
                content = [
                    Content(
                        mediaType = "application/json",
                        examples = [ExampleObject(value = """{"error":"충전 액수는 1 이상이어야 합니다."}""")]
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
            )
        ]
    )
    @PatchMapping("/{id}/balance/charge")
    fun chargeBalance(
        @PathVariable id: Long,
        @RequestBody(
            description = "충전할 금액",
            required = true,
            content = [
                Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = BalanceRequest.Charge::class),
                    examples = [ExampleObject(
                        value = """{"amount":50000}"""
                    )]
                )
            ]
        )
        request: BalanceRequest.Charge,
    ): ResponseEntity<BalanceResponse.Summary>

    @Operation(summary = "사용자 보유 쿠폰 조회")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "보유 쿠폰 목록 반환",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = CouponResponse.Owned::class),
                        examples = [ExampleObject(
                            value = """
                            [
                              {
                                "name": "첫 구매 할인",
                                "discountType": "FIXED",
                                "discountAmount": 3000,
                                "status": "ISSUED",
                                "issuedAt": "2025-04-02T15:00:00Z",
                                "expiredAt": "2025-04-30T23:59:59Z"
                              },
                              {
                                "name": "봄맞이 프로모션",
                                "discountType": "PERCENT",
                                "discountAmount": 10,
                                "status": "USED",
                                "issuedAt": "2025-03-20T11:30:00Z",
                                "expiredAt": "2025-04-10T23:59:59Z"
                              }
                            ]
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
            )
        ]
    )
    @GetMapping("/{id}/coupons")
    fun getCustomerCoupons(@PathVariable id: Long): ResponseEntity<List<CouponResponse.Owned>>
}

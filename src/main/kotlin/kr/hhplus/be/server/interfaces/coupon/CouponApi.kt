package kr.hhplus.be.server.interfaces.coupon

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping

@Tag(name = "Coupon", description = "쿠폰 관련 API")
@RequestMapping("/coupons")
interface CouponApi {
    @Operation(summary = "쿠폰 발급")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "쿠폰 발급 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = CouponResponse.Issue::class),
                        examples = [ExampleObject(
                            value = """
                    {
                      "couponId": 2001,
                      "customerId": 1,
                      "status": "ISSUED",
                      "issuedAt": "2025-04-02T15:00:00Z"
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
    @PostMapping("/issue")
    fun issue(
        @RequestBody(
            description = "쿠폰 발급 요청 정보",
            required = true,
            content = [
                Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = CouponRequest.Issue::class),
                    examples = [ExampleObject(
                        value = """
                        {
                          "couponId": 1,
                          "customerId": 1
                        }
                        """
                    )]
                )
            ]
        )
        request: CouponRequest.Issue
    ): ResponseEntity<CouponResponse.Issue>

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
    @GetMapping("/customer/{customerId}")
    fun getCustomerCoupons(@PathVariable customerId: Long): ResponseEntity<List<CouponResponse.Owned>>
}

package kr.hhplus.be.server.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import kr.hhplus.be.server.dto.CouponIssueRequest
import kr.hhplus.be.server.dto.CouponIssueResponse
import org.springframework.http.ResponseEntity
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
                        schema = Schema(implementation = CouponIssueResponse::class),
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
    @PostMapping("/{id}/issue")
    fun issue(
        @PathVariable id: Long,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "쿠폰 발급 요청 정보",
            required = true,
            content = [
                Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = CouponIssueRequest::class),
                    examples = [ExampleObject(
                        value = """
                        {
                          "customerId": 1
                        }
                        """
                    )]
                )
            ]
        )
        request: CouponIssueRequest
    ): ResponseEntity<CouponIssueResponse>
}

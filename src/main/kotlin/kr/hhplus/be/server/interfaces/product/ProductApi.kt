package kr.hhplus.be.server.interfaces.product

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam


@Tag(name = "Product", description = "상품 관련 API")
@RequestMapping("/products")
interface ProductApi {
    @Operation(summary = "상품 목록 조회")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "상품 목록 반환",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ProductResponse.Summary::class),
                        examples = [ExampleObject(
                            value = """
                            [
                              { "id": 1, "name": "청바지", "basePrice": 39000 },
                              { "id": 2, "name": "후드티", "basePrice": 29000 },
                              { "id": 3, "name": "운동화", "basePrice": 59000 }
                            ]
                            """
                        )]
                    )
                ]
            )
        ]
    )
    @GetMapping
    fun getAllProduct(): ResponseEntity<List<ProductResponse.Summary>>

    @Operation(summary = "상품 상세 조회")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "상품 상세 정보 반환",
            content = [
                Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ProductResponse.Detail::class),
                    examples = [ExampleObject(
                        value = """
                        {
                          "id": 1,
                          "name": "청바지",
                          "basePrice": 39000,
                          "options": [
                            { "optionName": "S", "extraPrice": 0 },
                            { "optionName": "M", "extraPrice": 1000 },
                            { "optionName": "L", "extraPrice": 2000 }
                          ]
                        }
                        """
                    )]
                )
            ]
        ),
        ApiResponse(
            responseCode = "404",
            description = "상품을 찾을 수 없음",
            content = [
                Content(
                    mediaType = "application/json",
                    examples = [ExampleObject(value = """{"error":"찾을 수 없는 상품입니다."}""")]
                )
            ]
        )
    )
    @GetMapping("/{id}")
    fun getProductDetail(@PathVariable id: Long): ResponseEntity<ProductResponse.Detail>

    @Operation(summary = "인기 상품 조회")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "기간 내 가장 많이 팔린 상품 목록",
            content = [
                Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ProductResponse.Popular::class),
                    examples = [ExampleObject(
                        value = """
                    [
                      {
                        "productId": 1,
                        "name": "청바지",
                        "basePrice": 39000,
                        "salesCount": 12
                      },
                      {
                        "productId": 2,
                        "name": "후드티",
                        "basePrice": 29000,
                        "salesCount": 9
                      },
                      {
                        "productId": 3,
                        "name": "운동화",
                        "basePrice": 59000,
                        "salesCount": 7
                      },
                      {
                        "productId": 4,
                        "name": "잠바",
                        "basePrice": 79000,
                        "salesCount": 5
                      },
                      {
                        "productId": 5,
                        "name": "실내화",
                        "basePrice": 15000,
                        "salesCount": 3
                      }
                    ]
                    """
                    )]
                )
            ]
        ),
        ApiResponse(
            responseCode = "404",
            description = "유효하지 않은 쿼리",
            content = [
                Content(
                    mediaType = "application/json",
                    examples = [ExampleObject(
                        value = """{"error":"days, weeks, months 쿼리로 조회 기간을 설정해야 합니다."}"""
                    )]
                )
            ]
        )
    )
    @GetMapping("/popular")
    fun getPopularProducts(
        @Parameter(description = "일 단위", example = "7") @RequestParam(required = false) days: Int?,
        @Parameter(description = "주 단위", example = "4") @RequestParam(required = false) weeks: Int?,
        @Parameter(description = "월 단위", example = "1") @RequestParam(required = false) months: Int?
    ): ResponseEntity<List<ProductResponse.Popular>>
}

package kr.hhplus.be.server.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import kr.hhplus.be.server.dto.ProductListResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping


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
                        schema = Schema(implementation = ProductListResponse::class),
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
    fun getProducts(): ResponseEntity<List<ProductListResponse>>
}

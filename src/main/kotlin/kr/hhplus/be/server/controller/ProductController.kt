package kr.hhplus.be.server.controller

import kr.hhplus.be.server.api.ProductApi
import kr.hhplus.be.server.dto.PopularProductResponse
import kr.hhplus.be.server.dto.ProductDetailResponse
import kr.hhplus.be.server.dto.ProductListResponse
import kr.hhplus.be.server.dto.ProductOptionResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class ProductController : ProductApi {
    override fun getAllProduct(): ResponseEntity<List<ProductListResponse>> {
        val response = listOf(
            ProductListResponse(
                id = 1L,
                name = "청바지",
                basePrice = 39000
            ),
            ProductListResponse(
                id = 2L,
                name = "후드티",
                basePrice = 29000
            ),
            ProductListResponse(
                id = 3L,
                name = "운동화",
                basePrice = 59000
            )
        )

        return ResponseEntity.ok(response)
    }

    override fun getProductDetail(id: Long): ResponseEntity<ProductDetailResponse> {
        val response = ProductDetailResponse(
            id = 1L,
            name = "청바지",
            basePrice = 39000,
            options = listOf(
                ProductOptionResponse(optionName = "S", extraPrice = 0),
                ProductOptionResponse(optionName = "M", extraPrice = 1000),
                ProductOptionResponse(optionName = "L", extraPrice = 2000)
            )
        )

        return ResponseEntity.ok(response)
    }

    override fun getPopularProducts(
        days: Int?,
        weeks: Int?,
        months: Int?
    ): ResponseEntity<List<PopularProductResponse>> {
        val response = listOf(
            PopularProductResponse(1, "청바지", 39000, 12),
            PopularProductResponse(2, "후드티", 29000, 9),
            PopularProductResponse(3, "운동화", 59000, 7),
            PopularProductResponse(4, "잠바", 79000, 5),
            PopularProductResponse(5, "실내화", 15000, 3)
        )

        return ResponseEntity.ok(response)
    }
}

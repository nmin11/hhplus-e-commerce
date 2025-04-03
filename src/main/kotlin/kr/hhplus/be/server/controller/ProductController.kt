package kr.hhplus.be.server.controller

import kr.hhplus.be.server.api.ProductApi
import kr.hhplus.be.server.dto.ProductDetailResponse
import kr.hhplus.be.server.dto.ProductListResponse
import kr.hhplus.be.server.dto.ProductOptionResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class ProductController : ProductApi {
    override fun getAllProduct(): ResponseEntity<List<ProductListResponse>> {
        return ResponseEntity.ok(listOf(
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
        ))
    }

    override fun getProductDetail(id: Long): ResponseEntity<ProductDetailResponse> {
        return ResponseEntity.ok(
            ProductDetailResponse(
                id = 1L,
                name = "청바지",
                basePrice = 39000,
                options = listOf(
                    ProductOptionResponse(optionName = "S", extraPrice = 0),
                    ProductOptionResponse(optionName = "M", extraPrice = 1000),
                    ProductOptionResponse(optionName = "L", extraPrice = 2000)
                )
            )
        )
    }
}

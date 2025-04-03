package kr.hhplus.be.server.controller

import kr.hhplus.be.server.api.ProductApi
import kr.hhplus.be.server.dto.ProductListResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class ProductController : ProductApi {
    override fun getProducts(): ResponseEntity<List<ProductListResponse>> {
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
}

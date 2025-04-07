package kr.hhplus.be.server.interfaces.product

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class ProductController : ProductApi {
    override fun getAllProduct(): ResponseEntity<List<ProductResponse.Summary>> {
        val response = listOf(
            ProductResponse.Summary(
                id = 1L,
                name = "청바지",
                basePrice = 39000
            ),
            ProductResponse.Summary(
                id = 2L,
                name = "후드티",
                basePrice = 29000
            ),
            ProductResponse.Summary(
                id = 3L,
                name = "운동화",
                basePrice = 59000
            )
        )

        return ResponseEntity.ok(response)
    }

    override fun getProductDetail(id: Long): ResponseEntity<ProductResponse.Detail> {
        val response = ProductResponse.Detail(
            id = 1L,
            name = "청바지",
            basePrice = 39000,
            options = listOf(
                ProductResponse.Option(name = "S", extraPrice = 0),
                ProductResponse.Option(name = "M", extraPrice = 1000),
                ProductResponse.Option(name = "L", extraPrice = 2000)
            )
        )

        return ResponseEntity.ok(response)
    }

    override fun getPopularProducts(
        @RequestParam(required = false) days: Int?,
        @RequestParam(required = false) weeks: Int?,
        @RequestParam(required = false) months: Int?
    ): ResponseEntity<List<ProductResponse.Popular>> {
        val response = listOf(
            ProductResponse.Popular(1, "청바지", 39000, 12),
            ProductResponse.Popular(2, "후드티", 29000, 9),
            ProductResponse.Popular(3, "운동화", 59000, 7),
            ProductResponse.Popular(4, "잠바", 79000, 5),
            ProductResponse.Popular(5, "실내화", 15000, 3)
        )

        return ResponseEntity.ok(response)
    }
}

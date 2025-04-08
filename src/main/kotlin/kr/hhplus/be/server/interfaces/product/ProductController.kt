package kr.hhplus.be.server.interfaces.product

import kr.hhplus.be.server.application.product.ProductCriteria
import kr.hhplus.be.server.application.product.ProductFacade
import kr.hhplus.be.server.domain.product.ProductService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class ProductController(
    private val productFacade: ProductFacade,
    private val productService: ProductService
) : ProductApi {
    override fun getAllProduct(): ResponseEntity<List<ProductResponse.Summary>> {
        val response = productService.getAll().map { ProductResponse.from(it) }
        return ResponseEntity.ok(response)
    }

    override fun getProductDetail(id: Long): ResponseEntity<ProductResponse.Detail> {
        val productDetail = productFacade.getProductDetail(id)
        val response = ProductResponse.from(productDetail.first, productDetail.second)
        return ResponseEntity.ok(response)
    }

    override fun getPopularProducts(
        @RequestParam(required = false) days: Int?,
        @RequestParam(required = false) weeks: Int?,
        @RequestParam(required = false) months: Int?
    ): ResponseEntity<List<ProductResponse.Popular>> {
        val result = productFacade.getPopularProducts(ProductCriteria.PeriodCondition(days, weeks, months))
        val response = result.map { ProductResponse.from(it) }
        return ResponseEntity.ok(response)
    }
}

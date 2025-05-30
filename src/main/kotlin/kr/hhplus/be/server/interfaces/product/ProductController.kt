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
        val response = productService.getAll().map { ProductResponse.Summary.from(it) }
        return ResponseEntity.ok(response)
    }

    override fun getProductDetail(id: Long): ResponseEntity<ProductResponse.Detail> {
        val result = productFacade.getProductDetail(id)
        val response = ProductResponse.Detail.from(result.product, result.options)
        return ResponseEntity.ok(response)
    }

    override fun getPopularProducts(
        @RequestParam(required = false) days: Int?,
        @RequestParam(required = false) weeks: Int?,
        @RequestParam(required = false) months: Int?
    ): ResponseEntity<List<ProductResponse.Popular>> {
        val result = productFacade.getPopularProducts(ProductCriteria.PeriodCondition(days, weeks, months))
        val response = result.map { ProductResponse.Popular.from(it) }
        return ResponseEntity.ok(response)
    }
}

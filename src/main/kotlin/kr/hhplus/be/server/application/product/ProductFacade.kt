package kr.hhplus.be.server.application.product

import kr.hhplus.be.server.domain.product.*
import org.springframework.stereotype.Component

@Component
class ProductFacade(
    private val productService: ProductService,
    private val productOptionService: ProductOptionService,
    private val statisticService: StatisticService
) {
    fun getProductDetail(productId: Long): ProductResult.Detail {
        val product = productService.getById(productId)
        val options = productOptionService.getByProductId(productId)
        return ProductResult.Detail(product, options)
    }

    fun getPopularProducts(condition: ProductCriteria.PeriodCondition): List<ProductResult.Popular> {
        val since = condition.toStartDate()
        val infos = statisticService.getTop5PopularProductStatistics(since)
        return infos.map { ProductResult.Popular.from(it) }
    }
}

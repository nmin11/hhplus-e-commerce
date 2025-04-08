package kr.hhplus.be.server.application.product

import kr.hhplus.be.server.domain.product.*
import org.springframework.stereotype.Component

@Component
class ProductFacade(
    private val productService: ProductService,
    private val productOptionService: ProductOptionService,
    private val statisticService: StatisticService
) {
    fun getProductDetail(productId: Long): Pair<Product, List<ProductOption>> {
        val product = productService.getById(productId)
        val options = productOptionService.getByProductId(productId)
        return Pair(product, options)
    }

    fun getPopularProducts(condition: ProductCriteria.PeriodCondition): List<ProductResult.Popular> {
        val since = condition.toStartDate()
        val statistics = statisticService.getTop5PopularProductStatistics(since)
        val productIds = statistics.map { it.productId }
        val products = productService.getAllByIds(productIds)
        val productMap = products.associateBy { it.id }

        return statistics.map { stat ->
            val product = productMap[stat.productId]
                ?: throw IllegalStateException("통계에 포함된 상품이 존재하지 않습니다.")

            ProductResult.Popular(
                productId = product.id!!,
                name = product.name,
                basePrice = product.basePrice,
                salesCount = stat.salesCount
            )
        }
    }
}

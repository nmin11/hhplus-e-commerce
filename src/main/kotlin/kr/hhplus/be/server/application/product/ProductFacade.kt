package kr.hhplus.be.server.application.product

import kr.hhplus.be.server.domain.product.*
import org.springframework.stereotype.Component

@Component
class ProductFacade(
    private val productService: ProductService,
    private val productOptionService: ProductOptionService,
    private val productRankService: ProductRankService
) {
    fun getProductDetail(productId: Long): ProductResult.Detail {
        val product = productService.getById(productId)
        val options = productOptionService.getByProductId(productId)
        return ProductResult.Detail(product, options)
    }

    fun getPopularProducts(condition: ProductCriteria.PeriodCondition): List<ProductResult.Popular> {
        val since = condition.toStartDate()
        val periodKey = condition.toPeriodKey()
        val infos = productRankService.getProductRanks(since, periodKey)
        val productIds = infos.map { it.productId }
        val products = productService.getAllByIds(productIds)
        val productMap = products.associateBy { it.id }

        return infos.mapNotNull { rank ->
            val product = productMap[rank.productId] ?: return@mapNotNull null
            ProductResult.Popular.from(rank, product)
        }
    }
}

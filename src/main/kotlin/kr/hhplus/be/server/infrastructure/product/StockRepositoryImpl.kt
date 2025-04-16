package kr.hhplus.be.server.infrastructure.product

import kr.hhplus.be.server.domain.product.Stock
import kr.hhplus.be.server.domain.product.StockRepository
import org.springframework.stereotype.Repository

@Repository
class StockRepositoryImpl(
    private val stockJpaRepository: StockJpaRepository
) : StockRepository {
    override fun save(stock: Stock): Stock {
        return stockJpaRepository.save(stock)
    }

    override fun findByProductOptionId(productOptionId: Long): Stock? {
        return stockJpaRepository.findByProductOptionId(productOptionId)
    }
}

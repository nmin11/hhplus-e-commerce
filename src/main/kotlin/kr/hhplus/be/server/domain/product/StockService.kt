package kr.hhplus.be.server.domain.product

import kr.hhplus.be.server.support.exception.product.StockInsufficientException
import kr.hhplus.be.server.support.exception.product.StockNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class StockService(
    private val stockRepository: StockRepository
) {
    fun getByProductOptionId(productOptionId: Long): Stock {
        return stockRepository.findByProductOptionId(productOptionId)
            ?: throw StockNotFoundException()
    }

    fun validate(productOptionId: Long, requiredQuantity: Int) {
        val stock = getByProductOptionId(productOptionId)
        if (stock.quantity < requiredQuantity) {
            throw StockInsufficientException()
        }
    }

    @Transactional
    fun decrease(productOptionId: Long, quantity: Int) {
        val stock = getByProductOptionIdWithLock(productOptionId)
        stock.decrease(quantity)
        stockRepository.save(stock)
    }

    private fun getByProductOptionIdWithLock(productOptionId: Long): Stock {
        return stockRepository.findByProductOptionIdWithLock(productOptionId)
            ?: throw StockNotFoundException()
    }
}

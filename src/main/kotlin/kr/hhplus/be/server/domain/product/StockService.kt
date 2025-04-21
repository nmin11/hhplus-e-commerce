package kr.hhplus.be.server.domain.product

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class StockService(
    private val stockRepository: StockRepository
) {
    fun getByProductOptionId(productOptionId: Long): Stock {
        return stockRepository.findByProductOptionId(productOptionId)
            ?: throw IllegalArgumentException("재고 정보가 존재하지 않습니다.")
    }

    fun validate(productOptionId: Long, requiredQuantity: Int) {
        val stock = getByProductOptionId(productOptionId)
        if (stock.quantity < requiredQuantity) {
            throw IllegalStateException("재고가 ${stock.quantity}개 남아 있어서 주문이 불가능합니다.")
        }
    }

    @Transactional
    fun decrease(productOptionId: Long, quantity: Int) {
        val stock = getWithLockByProductOptionId(productOptionId)
        stock.decrease(quantity)
        stockRepository.save(stock)
    }

    private fun getWithLockByProductOptionId(productOptionId: Long): Stock {
        return stockRepository.findWithLockByProductOptionId(productOptionId)
            ?: throw IllegalArgumentException("재고 정보가 존재하지 않습니다.")
    }
}

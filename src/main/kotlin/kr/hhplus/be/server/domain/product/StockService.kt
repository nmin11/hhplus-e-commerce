package kr.hhplus.be.server.domain.product

import org.springframework.stereotype.Service

@Service
class StockService(
    private val stockRepository: StockRepository
) {
    fun validate(productOptionId: Long, requiredQuantity: Int) {
        val stock = stockRepository.findByProductOptionId(productOptionId)
            ?: throw IllegalArgumentException("재고 정보가 존재하지 않습니다.")

        if (stock.quantity < requiredQuantity) {
            throw IllegalStateException("재고가 ${stock.quantity}개 남아 있어서 주문이 불가능합니다.")
        }
    }
}

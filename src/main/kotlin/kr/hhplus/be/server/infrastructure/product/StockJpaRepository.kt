package kr.hhplus.be.server.infrastructure.product

import jakarta.persistence.LockModeType
import kr.hhplus.be.server.domain.product.Stock
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query

interface StockJpaRepository : JpaRepository<Stock, Long> {
    fun findByProductOptionId(productOptionId: Long): Stock?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Stock s WHERE s.productOption.id = :productOptionId")
    fun findByProductOptionIdWithLock(productOptionId: Long): Stock?
}

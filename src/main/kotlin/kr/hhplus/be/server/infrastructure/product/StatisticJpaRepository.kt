package kr.hhplus.be.server.infrastructure.product

import kr.hhplus.be.server.domain.product.Statistic
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

interface StatisticJpaRepository : JpaRepository<Statistic, Long> {
    @Query(value =
        """
            SELECT
                p.id AS id,
                p.name AS name,
                p.base_price AS basePrice,
                SUM(s.sales_count) AS totalSales
            FROM statistic s
            JOIN product p ON s.product_id = p.id
            WHERE s.sold_at > :since
            GROUP BY p.id, p.name, p.base_price
            ORDER BY SUM(s.sales_count) DESC
            LIMIT 5
        """,
        nativeQuery = true
    )
    fun findTop5PopularProducts(since: LocalDateTime): List<PopularProductProjection>
}

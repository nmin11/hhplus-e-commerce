package kr.hhplus.be.server.infrastructure.product

import kr.hhplus.jooq.Tables.PRODUCT
import kr.hhplus.jooq.Tables.STATISTIC
import org.jooq.DSLContext
import org.jooq.impl.DSL.field
import org.jooq.impl.DSL.sum
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class StatisticJooqRepository(
    private val dsl: DSLContext
) {
    fun findTop5ProductSales(since: LocalDateTime): List<PopularProductRecord> {
        val stat = STATISTIC
        val prod = PRODUCT

        // 1. 주어진 날짜 이후 TOP 5 판매 상품 ID 조회 (Subquery)
        val subquery = dsl
            .select(stat.PRODUCT_ID, sum(stat.SALES_COUNT).`as`("total_sales"))
            .from(stat)
            .where(stat.SOLD_AT.gt(since))
            .groupBy(stat.PRODUCT_ID)
            .orderBy(field("total_sales").desc())
            .limit(5)
        val s = subquery.asTable("s")

        // 2. 상품 정보 조회 및 집계 결과 매핑 (Main Query)
        return dsl
            .select(
                prod.ID.`as`("id"),
                prod.NAME.`as`("name"),
                prod.BASE_PRICE.`as`("basePrice"),
                field("s.total_sales", Int::class.java).`as`("totalSales")
            )
            .from(s)
            .join(prod).on(requireNotNull(s.field("product_id", Long::class.java)).eq(prod.ID))
            .fetchInto(PopularProductRecord::class.java)
    }
}

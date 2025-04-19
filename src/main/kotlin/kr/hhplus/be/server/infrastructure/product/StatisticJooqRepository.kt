package kr.hhplus.be.server.infrastructure.product

import org.jooq.DSLContext
import org.jooq.impl.DSL.field
import org.jooq.impl.DSL.table
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class StatisticJooqRepository(
    private val dsl: DSLContext
) {
    // TODO: jooq-codegen 방식으로의 변경 필요
    fun findTop5ProductSales(since: LocalDateTime): List<PopularProductRecord> {
        return dsl
            .select(
                field("p.id", Long::class.java).`as`("id"),
                field("p.name", String::class.java).`as`("name"),
                field("p.base_price", Int::class.java).`as`("basePrice"),
                field("sum(s.sales_count)", Int::class.java).`as`("totalSales")
            )
            .from(table("statistic").`as`("s"))
            .join(table("product").`as`("p"))
            .on(field("s.product_id").eq(field("p.id")))
            .where(field("s.sold_at", LocalDateTime::class.java).gt(since))
            .groupBy(
                field("p.id"),
                field("p.name"),
                field("p.base_price")
            )
            .orderBy(field("totalSales").desc())
            .limit(5)
            .fetch()
            .map {
                PopularProductRecord(
                    id = it.get("id", Long::class.java)!!,
                    name = it.get("name", String::class.java)!!,
                    basePrice = it.get("basePrice", Int::class.java)!!,
                    totalSales = it.get("totalSales", Int::class.java)!!
                )
            }
    }
}

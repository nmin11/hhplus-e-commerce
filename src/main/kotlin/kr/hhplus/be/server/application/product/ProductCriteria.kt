package kr.hhplus.be.server.application.product

import java.time.LocalDate

sealed class ProductCriteria {
    data class PeriodCondition(
        val days: Int? = null,
        val weeks: Int? = null,
        val months: Int? = null
    ) {
        fun toStartDate(): LocalDate {
            val now = LocalDate.now()
            return when {
                days != null -> now.minusDays(days.toLong())
                weeks != null -> now.minusWeeks(weeks.toLong())
                months != null -> now.minusMonths(months.toLong())
                else -> now.minusDays(3)
            }
        }
    }
}

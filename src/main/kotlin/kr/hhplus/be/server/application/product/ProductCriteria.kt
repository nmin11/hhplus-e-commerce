package kr.hhplus.be.server.application.product

import kr.hhplus.be.server.support.exception.product.StatisticInvalidPeriodConditionException
import java.time.LocalDate

sealed class ProductCriteria {
    data class PeriodCondition(
        val days: Int? = null,
        val weeks: Int? = null,
        val months: Int? = null
    ) {
        init {
            val nonNullCount = listOf(days, weeks, months).count { it != null }
            if (nonNullCount != 1) {
                throw StatisticInvalidPeriodConditionException()
            }
        }

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

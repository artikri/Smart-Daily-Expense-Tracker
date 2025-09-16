package com.example.expensetracker.domain.model

import java.time.LocalDate

/**
 * Domain model for expense reports and analytics
 */
data class ExpenseReport(
    val period: ReportPeriod,
    val dailyTotals: List<DailyTotal>,
    val categoryTotals: List<CategoryTotal>,
    val totalAmount: Double,
    val totalCount: Int
)

/**
 * Daily expense total for a specific date
 */
data class DailyTotal(
    val date: LocalDate,
    val amount: Double,
    val count: Int
)

/**
 * Category-wise expense total
 */
data class CategoryTotal(
    val category: ExpenseCategory,
    val amount: Double,
    val count: Int,
    val percentage: Double
)

/**
 * Report period types
 */
enum class ReportPeriod {
    TODAY,
    LAST_7_DAYS,
    LAST_30_DAYS,
    CUSTOM
}

/**
 * Filter options for expense list
 */
data class ExpenseFilter(
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val category: ExpenseCategory? = null,
    val groupBy: GroupBy = GroupBy.TIME
)

/**
 * Grouping options for expense list
 */
enum class GroupBy {
    TIME,      // Group by date
    CATEGORY   // Group by category
}

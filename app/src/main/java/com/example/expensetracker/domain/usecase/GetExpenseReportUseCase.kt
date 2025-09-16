package com.example.expensetracker.domain.usecase

import com.example.expensetracker.domain.model.ExpenseReport
import com.example.expensetracker.domain.model.ReportPeriod
import com.example.expensetracker.domain.repository.ExpenseRepository
import java.time.LocalDate
import javax.inject.Inject

/**
 * Use case for generating expense reports and analytics
 */
class GetExpenseReportUseCase @Inject constructor(
    private val repository: ExpenseRepository
) {
    
    suspend fun getReport(period: ReportPeriod): ExpenseReport {
        val (startDate, endDate) = when (period) {
            ReportPeriod.TODAY -> {
                val today = LocalDate.now()
                today to today
            }
            ReportPeriod.LAST_7_DAYS -> {
                val endDate = LocalDate.now()
                val startDate = endDate.minusDays(6)
                startDate to endDate
            }
            ReportPeriod.LAST_30_DAYS -> {
                val endDate = LocalDate.now()
                val startDate = endDate.minusDays(29)
                startDate to endDate
            }
            ReportPeriod.CUSTOM -> {
                // This should be handled by the caller with specific dates
                val today = LocalDate.now()
                today to today
            }
        }
        
        return repository.getExpenseReport(startDate, endDate).copy(period = period)
    }
    
    suspend fun getCustomReport(startDate: LocalDate, endDate: LocalDate): ExpenseReport {
        return repository.getExpenseReport(startDate, endDate).copy(period = ReportPeriod.CUSTOM)
    }
}

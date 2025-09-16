package com.example.expensetracker.domain.usecase

import com.example.expensetracker.domain.repository.ExpenseRepository
import javax.inject.Inject

/**
 * Use case for getting today's expense summary
 * Provides real-time "Total Spent Today" functionality
 */
class GetTodaySummaryUseCase @Inject constructor(
    private val repository: ExpenseRepository
) {
    
    suspend operator fun invoke(): TodaySummary {
        val totalAmount = repository.getTodayTotalAmount()
        val expenseCount = repository.getTodayExpenseCount()
        
        return TodaySummary(
            totalAmount = totalAmount,
            expenseCount = expenseCount
        )
    }
}

/**
 * Data class representing today's expense summary
 */
data class TodaySummary(
    val totalAmount: Double,
    val expenseCount: Int
)

package com.example.expensetracker.domain.usecase

import com.example.expensetracker.domain.model.Expense
import com.example.expensetracker.domain.model.ExpenseFilter
import com.example.expensetracker.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

/**
 * Use case for retrieving expenses with various filters
 */
class GetExpensesUseCase @Inject constructor(
    private val repository: ExpenseRepository
) {
    
    fun getAllExpenses(): Flow<List<Expense>> {
        return repository.getAllExpenses()
    }
    
    fun getExpensesByDate(date: LocalDate): Flow<List<Expense>> {
        return repository.getExpensesByDate(date)
    }
    
    fun getExpensesByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Expense>> {
        return repository.getExpensesByDateRange(startDate, endDate)
    }
    
    fun getExpensesByFilter(filter: ExpenseFilter): Flow<List<Expense>> {
        return repository.getExpensesByFilter(filter)
    }
    
    fun getTodayExpenses(): Flow<List<Expense>> {
        return repository.getTodayExpenses()
    }
}

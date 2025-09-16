package com.example.expensetracker.domain.repository

import com.example.expensetracker.domain.model.Expense
import com.example.expensetracker.domain.model.ExpenseCategory
import com.example.expensetracker.domain.model.ExpenseFilter
import com.example.expensetracker.domain.model.ExpenseReport
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Repository interface for expense operations
 * Defines the contract for data access in the domain layer
 */
interface ExpenseRepository {
    
    // Basic CRUD operations
    suspend fun addExpense(expense: Expense): Long
    suspend fun updateExpense(expense: Expense)
    suspend fun deleteExpense(expense: Expense)
    suspend fun getExpenseById(id: Long): Expense?
    
    // Flow-based operations for reactive UI
    fun getAllExpenses(): Flow<List<Expense>>
    fun getExpensesByDate(date: LocalDate): Flow<List<Expense>>
    fun getExpensesByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Expense>>
    fun getExpensesByCategory(category: ExpenseCategory): Flow<List<Expense>>
    fun getExpensesByFilter(filter: ExpenseFilter): Flow<List<Expense>>
    
    // Analytics and reporting
    suspend fun getTotalAmountByDate(date: LocalDate): Double
    suspend fun getExpenseCountByDate(date: LocalDate): Int
    suspend fun getTotalAmountByDateRange(startDate: LocalDate, endDate: LocalDate): Double
    suspend fun getExpenseCountByDateRange(startDate: LocalDate, endDate: LocalDate): Int
    suspend fun getExpenseReport(startDate: LocalDate, endDate: LocalDate): ExpenseReport
    
    // Duplicate detection
    suspend fun findDuplicateExpense(title: String, amount: Double, date: LocalDate): Expense?
    
    // Today's summary
    suspend fun getTodayTotalAmount(): Double
    suspend fun getTodayExpenseCount(): Int
    fun getTodayExpenses(): Flow<List<Expense>>
}

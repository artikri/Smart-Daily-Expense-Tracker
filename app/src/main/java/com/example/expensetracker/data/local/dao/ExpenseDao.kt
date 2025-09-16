package com.example.expensetracker.data.local.dao

import androidx.room.*
import com.example.expensetracker.data.local.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Data Access Object for Expense operations
 * Defines all database operations for expenses
 */
@Dao
interface ExpenseDao {
    
    @Query("SELECT * FROM expenses ORDER BY createdAt DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>
    
    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getExpenseById(id: Long): ExpenseEntity?
    
    @Query("SELECT * FROM expenses WHERE date(createdAt) = date(:date) ORDER BY createdAt DESC")
    fun getExpensesByDate(date: String): Flow<List<ExpenseEntity>>
    
    @Query("SELECT * FROM expenses WHERE date(createdAt) BETWEEN date(:startDate) AND date(:endDate) ORDER BY createdAt DESC")
    fun getExpensesByDateRange(startDate: String, endDate: String): Flow<List<ExpenseEntity>>
    
    @Query("SELECT * FROM expenses WHERE category = :category ORDER BY createdAt DESC")
    fun getExpensesByCategory(category: String): Flow<List<ExpenseEntity>>
    
    @Query("SELECT * FROM expenses WHERE date(createdAt) = date(:date) AND category = :category ORDER BY createdAt DESC")
    fun getExpensesByDateAndCategory(date: String, category: String): Flow<List<ExpenseEntity>>
    
    @Query("SELECT SUM(amount) FROM expenses WHERE date(createdAt) = date(:date)")
    suspend fun getTotalAmountByDate(date: String): Double?
    
    @Query("SELECT COUNT(*) FROM expenses WHERE date(createdAt) = date(:date)")
    suspend fun getExpenseCountByDate(date: String): Int
    
    @Query("SELECT SUM(amount) FROM expenses WHERE date(createdAt) BETWEEN date(:startDate) AND date(:endDate)")
    suspend fun getTotalAmountByDateRange(startDate: String, endDate: String): Double?
    
    @Query("SELECT COUNT(*) FROM expenses WHERE date(createdAt) BETWEEN date(:startDate) AND date(:endDate)")
    suspend fun getExpenseCountByDateRange(startDate: String, endDate: String): Int
    
    @Query("SELECT category, SUM(amount) as totalAmount, COUNT(*) as count FROM expenses WHERE date(createdAt) BETWEEN date(:startDate) AND date(:endDate) GROUP BY category")
    suspend fun getCategoryTotalsByDateRange(startDate: String, endDate: String): List<CategoryTotalResult>
    
    @Query("SELECT date(createdAt) as date, SUM(amount) as totalAmount, COUNT(*) as count FROM expenses WHERE date(createdAt) BETWEEN date(:startDate) AND date(:endDate) GROUP BY date(createdAt) ORDER BY date(createdAt)")
    suspend fun getDailyTotalsByDateRange(startDate: String, endDate: String): List<DailyTotalResult>
    
    @Query("SELECT * FROM expenses WHERE title = :title AND amount = :amount AND date(createdAt) = date(:date)")
    suspend fun findDuplicateExpense(title: String, amount: Double, date: String): ExpenseEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long
    
    @Update
    suspend fun updateExpense(expense: ExpenseEntity)
    
    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)
    
    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteExpenseById(id: Long)
    
    @Query("DELETE FROM expenses")
    suspend fun deleteAllExpenses()
}

/**
 * Result class for category totals query
 */
data class CategoryTotalResult(
    val category: String,
    val totalAmount: Double,
    val count: Int
)

/**
 * Result class for daily totals query
 */
data class DailyTotalResult(
    val date: String,
    val totalAmount: Double,
    val count: Int
)

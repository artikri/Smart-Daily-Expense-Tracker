package com.example.expensetracker.data.repository

import com.example.expensetracker.data.local.ExpenseDatabase
import com.example.expensetracker.data.local.dao.CategoryTotalResult
import com.example.expensetracker.data.local.dao.DailyTotalResult
import com.example.expensetracker.data.local.entity.ExpenseEntity
import com.example.expensetracker.domain.model.*
import com.example.expensetracker.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of ExpenseRepository
 * Handles data operations between domain and data layers
 */
@Singleton
class ExpenseRepositoryImpl @Inject constructor(
    private val database: ExpenseDatabase
) : ExpenseRepository {
    
    private val expenseDao = database.expenseDao()
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    
    override suspend fun addExpense(expense: Expense): Long {
        return expenseDao.insertExpense(ExpenseEntity.fromDomain(expense))
    }
    
    override suspend fun updateExpense(expense: Expense) {
        expenseDao.updateExpense(ExpenseEntity.fromDomain(expense))
    }
    
    override suspend fun deleteExpense(expense: Expense) {
        expenseDao.deleteExpense(ExpenseEntity.fromDomain(expense))
    }
    
    override suspend fun getExpenseById(id: Long): Expense? {
        return expenseDao.getExpenseById(id)?.toDomain()
    }
    
    override fun getAllExpenses(): Flow<List<Expense>> {
        return expenseDao.getAllExpenses().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getExpensesByDate(date: LocalDate): Flow<List<Expense>> {
        return expenseDao.getExpensesByDate(date.format(dateFormatter)).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getExpensesByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Expense>> {
        return expenseDao.getExpensesByDateRange(
            startDate.format(dateFormatter),
            endDate.format(dateFormatter)
        ).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getExpensesByCategory(category: ExpenseCategory): Flow<List<Expense>> {
        return expenseDao.getExpensesByCategory(category.name).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getExpensesByFilter(filter: ExpenseFilter): Flow<List<Expense>> {
        return when {
            filter.startDate != null && filter.endDate != null && filter.category != null -> {
                expenseDao.getExpensesByDateAndCategory(
                    filter.startDate.format(dateFormatter),
                    filter.category.name
                ).map { entities ->
                    entities.filter { 
                        val expenseDate = LocalDate.parse(it.createdAt.substring(0, 10))
                        expenseDate.isAfter(filter.startDate.minusDays(1)) && 
                        expenseDate.isBefore(filter.endDate.plusDays(1))
                    }.map { it.toDomain() }
                }
            }
            filter.startDate != null && filter.endDate != null -> {
                getExpensesByDateRange(filter.startDate, filter.endDate)
            }
            filter.category != null -> {
                getExpensesByCategory(filter.category)
            }
            else -> {
                getAllExpenses()
            }
        }
    }
    
    override suspend fun getTotalAmountByDate(date: LocalDate): Double {
        return expenseDao.getTotalAmountByDate(date.format(dateFormatter)) ?: 0.0
    }
    
    override suspend fun getExpenseCountByDate(date: LocalDate): Int {
        return expenseDao.getExpenseCountByDate(date.format(dateFormatter))
    }
    
    override suspend fun getTotalAmountByDateRange(startDate: LocalDate, endDate: LocalDate): Double {
        return expenseDao.getTotalAmountByDateRange(
            startDate.format(dateFormatter),
            endDate.format(dateFormatter)
        ) ?: 0.0
    }
    
    override suspend fun getExpenseCountByDateRange(startDate: LocalDate, endDate: LocalDate): Int {
        return expenseDao.getExpenseCountByDateRange(
            startDate.format(dateFormatter),
            endDate.format(dateFormatter)
        )
    }
    
    override suspend fun getExpenseReport(startDate: LocalDate, endDate: LocalDate): ExpenseReport {
        val dailyTotalsResult = expenseDao.getDailyTotalsByDateRange(
            startDate.format(dateFormatter),
            endDate.format(dateFormatter)
        )
        
        val categoryTotalsResult = expenseDao.getCategoryTotalsByDateRange(
            startDate.format(dateFormatter),
            endDate.format(dateFormatter)
        )
        
        val totalAmount = getTotalAmountByDateRange(startDate, endDate)
        val totalCount = getExpenseCountByDateRange(startDate, endDate)
        
        val dailyTotals = dailyTotalsResult.map { result ->
            DailyTotal(
                date = LocalDate.parse(result.date),
                amount = result.totalAmount,
                count = result.count
            )
        }
        
        val categoryTotals = categoryTotalsResult.map { result ->
            val category = ExpenseCategory.valueOf(result.category)
            CategoryTotal(
                category = category,
                amount = result.totalAmount,
                count = result.count,
                percentage = if (totalAmount > 0) (result.totalAmount / totalAmount) * 100 else 0.0
            )
        }
        
        return ExpenseReport(
            period = ReportPeriod.CUSTOM,
            dailyTotals = dailyTotals,
            categoryTotals = categoryTotals,
            totalAmount = totalAmount,
            totalCount = totalCount
        )
    }
    
    override suspend fun findDuplicateExpense(title: String, amount: Double, date: LocalDate): Expense? {
        return expenseDao.findDuplicateExpense(title, amount, date.format(dateFormatter))?.toDomain()
    }
    
    override suspend fun getTodayTotalAmount(): Double {
        return getTotalAmountByDate(LocalDate.now())
    }
    
    override suspend fun getTodayExpenseCount(): Int {
        return getExpenseCountByDate(LocalDate.now())
    }
    
    override fun getTodayExpenses(): Flow<List<Expense>> {
        return getExpensesByDate(LocalDate.now())
    }
}

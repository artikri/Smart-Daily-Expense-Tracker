package com.example.expensetracker.domain.usecase

import com.example.expensetracker.domain.model.Expense
import com.example.expensetracker.domain.repository.ExpenseRepository
import javax.inject.Inject

/**
 * Use case for adding a new expense
 * Contains business logic for expense creation
 */
class AddExpenseUseCase @Inject constructor(
    private val repository: ExpenseRepository
) {
    
    suspend operator fun invoke(expense: Expense): Result<Long> {
        return try {
            // Business logic validation
            if (expense.title.isBlank()) {
                return Result.failure(IllegalArgumentException("Title cannot be empty"))
            }
            
            if (expense.amount <= 0) {
                return Result.failure(IllegalArgumentException("Amount must be greater than 0"))
            }
            
            // Check for duplicates
            val duplicate = repository.findDuplicateExpense(
                title = expense.title,
                amount = expense.amount,
                date = expense.createdAt.toLocalDate()
            )
            
            if (duplicate != null) {
                return Result.failure(IllegalArgumentException("Duplicate expense found"))
            }
            
            // Add the expense
            val id = repository.addExpense(expense)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

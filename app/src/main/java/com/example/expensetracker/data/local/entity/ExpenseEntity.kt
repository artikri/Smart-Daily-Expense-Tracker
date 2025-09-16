package com.example.expensetracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.expensetracker.domain.model.ExpenseCategory
import java.time.LocalDateTime

/**
 * Room entity for Expense
 * Maps to the database table
 */
@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val category: String, // Store as string, convert to enum
    val notes: String? = null,
    val receiptImagePath: String? = null,
    val createdAt: String, // Store as ISO string
    val updatedAt: String
) {
    companion object {
        fun fromDomain(expense: com.example.expensetracker.domain.model.Expense): ExpenseEntity {
            return ExpenseEntity(
                id = expense.id,
                title = expense.title,
                amount = expense.amount,
                category = expense.category.name,
                notes = expense.notes,
                receiptImagePath = expense.receiptImagePath,
                createdAt = expense.createdAt.toString(),
                updatedAt = expense.updatedAt.toString()
            )
        }
    }
    
    fun toDomain(): com.example.expensetracker.domain.model.Expense {
        return com.example.expensetracker.domain.model.Expense(
            id = id,
            title = title,
            amount = amount,
            category = ExpenseCategory.valueOf(category),
            notes = notes,
            receiptImagePath = receiptImagePath,
            createdAt = LocalDateTime.parse(createdAt),
            updatedAt = LocalDateTime.parse(updatedAt)
        )
    }
}

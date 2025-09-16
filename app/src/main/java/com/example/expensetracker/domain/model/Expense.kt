package com.example.expensetracker.domain.model

import java.time.LocalDateTime

/**
 * Domain model for Expense entity
 * Represents a business expense with all necessary information
 */
data class Expense(
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val category: ExpenseCategory,
    val notes: String? = null,
    val receiptImagePath: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

/**
 * Enum representing different expense categories
 * Based on requirements: Staff, Travel, Food, Utility
 */
enum class ExpenseCategory(
    val displayName: String,
    val icon: String
) {
    STAFF("Staff", "👥"),
    TRAVEL("Travel", "✈️"),
    FOOD("Food", "🍽️"),
    UTILITY("Utility", "⚡");
    
    companion object {
        fun fromDisplayName(displayName: String): ExpenseCategory? {
            return values().find { it.displayName == displayName }
        }
    }
}

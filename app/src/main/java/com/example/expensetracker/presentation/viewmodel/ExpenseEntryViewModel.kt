package com.example.expensetracker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.domain.model.Expense
import com.example.expensetracker.domain.model.ExpenseCategory
import com.example.expensetracker.domain.usecase.AddExpenseUseCase
import com.example.expensetracker.domain.usecase.GetTodaySummaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * ViewModel for the Expense Entry Screen
 * Manages the state and business logic for adding new expenses
 */
@HiltViewModel
class ExpenseEntryViewModel @Inject constructor(
    private val addExpenseUseCase: AddExpenseUseCase,
    private val getTodaySummaryUseCase: GetTodaySummaryUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ExpenseEntryUiState())
    val uiState: StateFlow<ExpenseEntryUiState> = _uiState.asStateFlow()
    
    init {
        loadTodaySummary()
    }
    
    fun updateTitle(title: String) {
        _uiState.value = _uiState.value.copy(
            title = title,
            titleError = null
        )
    }
    
    fun updateAmount(amount: String) {
        _uiState.value = _uiState.value.copy(
            amount = amount,
            amountError = null
        )
    }
    
    fun updateCategory(category: ExpenseCategory) {
        _uiState.value = _uiState.value.copy(category = category)
    }
    
    fun updateNotes(notes: String) {
        _uiState.value = _uiState.value.copy(notes = notes)
    }
    
    fun addExpense() {
        val currentState = _uiState.value
        
        // Validate input
        val titleError = if (currentState.title.isBlank()) "Title cannot be empty" else null
        val amountError = try {
            val amount = currentState.amount.toDouble()
            if (amount <= 0) "Amount must be greater than 0" else null
        } catch (e: NumberFormatException) {
            "Please enter a valid amount"
        }
        
        if (titleError != null || amountError != null) {
            _uiState.value = currentState.copy(
                titleError = titleError,
                amountError = amountError
            )
            return
        }
        
        _uiState.value = currentState.copy(isLoading = true, error = null)
        
        viewModelScope.launch {
            try {
                val expense = Expense(
                    title = currentState.title.trim(),
                    amount = currentState.amount.toDouble(),
                    category = currentState.category,
                    notes = currentState.notes.takeIf { it.isNotBlank() },
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                )
                
                val result = addExpenseUseCase(expense)
                result.fold(
                    onSuccess = { id ->
                        _uiState.value = currentState.copy(
                            isLoading = false,
                            isSuccess = true,
                            successMessage = "Expense added successfully!",
                            title = "",
                            amount = "",
                            notes = ""
                        )
                        loadTodaySummary()
                    },
                    onFailure = { exception ->
                        _uiState.value = currentState.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to add expense"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    isLoading = false,
                    error = e.message ?: "An unexpected error occurred"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun clearSuccess() {
        _uiState.value = _uiState.value.copy(isSuccess = false, successMessage = null)
    }
    
    private fun loadTodaySummary() {
        viewModelScope.launch {
            try {
                val summary = getTodaySummaryUseCase()
                _uiState.value = _uiState.value.copy(
                    todayTotalAmount = summary.totalAmount,
                    todayExpenseCount = summary.expenseCount
                )
            } catch (e: Exception) {
                // Handle error silently for summary
            }
        }
    }
}

/**
 * UI State for the Expense Entry Screen
 */
data class ExpenseEntryUiState(
    val title: String = "",
    val amount: String = "",
    val category: ExpenseCategory = ExpenseCategory.FOOD,
    val notes: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val successMessage: String? = null,
    val error: String? = null,
    val titleError: String? = null,
    val amountError: String? = null,
    val todayTotalAmount: Double = 0.0,
    val todayExpenseCount: Int = 0
)

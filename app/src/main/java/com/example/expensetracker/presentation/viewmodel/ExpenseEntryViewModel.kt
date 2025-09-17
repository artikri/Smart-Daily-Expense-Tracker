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
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
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

    fun updateSelectedDate(date: LocalDate) {
        _uiState.value = _uiState.value.copy(
            selectedDate = date,
            dateError = null
        )
    }

    fun updateReceiptImageUri(uri: String?) {
        _uiState.value = _uiState.value.copy(receiptImageUri = uri)
    }
    
    fun addExpense() {
        val currentState = _uiState.value
        
        // Date cannot be in future
        val today = LocalDate.now()
        if (currentState.selectedDate.isAfter(today)) {
            _uiState.value = currentState.copy(
                error = "Date cannot be in the future",
                dateError = "Date cannot be in the future",
                isSuccess = false,
                successMessage = null
            )
            return
        }
        
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
                val created = LocalDateTime.of(currentState.selectedDate, LocalTime.now())
                val expense = Expense(
                    title = currentState.title.trim(),
                    amount = currentState.amount.toDouble(),
                    category = currentState.category,
                    notes = currentState.notes.takeIf { it.isNotBlank() },
                    receiptImagePath = currentState.receiptImageUri,
                    createdAt = created,
                    updatedAt = created
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
                            notes = "",
                            receiptImageUri = null,
                            dateError = null
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
        _uiState.value = _uiState.value.copy(error = null, dateError = null)
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
    val receiptImageUri: String? = null,
    val selectedDate: LocalDate = LocalDate.now(),
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val successMessage: String? = null,
    val error: String? = null,
    val titleError: String? = null,
    val amountError: String? = null,
    val dateError: String? = null,
    val todayTotalAmount: Double = 0.0,
    val todayExpenseCount: Int = 0
)

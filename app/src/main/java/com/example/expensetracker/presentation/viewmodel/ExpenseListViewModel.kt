package com.example.expensetracker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.domain.model.Expense
import com.example.expensetracker.domain.model.ExpenseCategory
import com.example.expensetracker.domain.model.ExpenseFilter
import com.example.expensetracker.domain.model.GroupBy
import com.example.expensetracker.domain.usecase.GetExpensesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * ViewModel for the Expense List Screen
 * Manages the state and business logic for displaying expenses
 */
@HiltViewModel
class ExpenseListViewModel @Inject constructor(
    private val getExpensesUseCase: GetExpensesUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ExpenseListUiState())
    val uiState: StateFlow<ExpenseListUiState> = _uiState.asStateFlow()
    
    init {
        loadExpenses()
    }
    
    fun loadExpenses() {
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                val filter = ExpenseFilter(
                    startDate = currentState.selectedStartDate,
                    endDate = currentState.selectedEndDate,
                    category = currentState.selectedCategory,
                    groupBy = currentState.groupBy
                )
                
                getExpensesUseCase.getExpensesByFilter(filter).collect { expenses ->
                    val groupedExpenses: Map<Any, List<Expense>> = if (currentState.groupBy == GroupBy.CATEGORY) {
                        expenses.groupBy { it.category as Any }
                    } else {
                        expenses.groupBy { it.createdAt.toLocalDate() as Any }
                    }
                    
                    _uiState.value = currentState.copy(
                        expenses = expenses,
                        groupedExpenses = groupedExpenses,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load expenses"
                )
            }
        }
    }
    
    fun updateDateFilter(startDate: LocalDate?, endDate: LocalDate?) {
        _uiState.value = _uiState.value.copy(
            selectedStartDate = startDate,
            selectedEndDate = endDate
        )
        loadExpenses()
    }
    
    fun updateCategoryFilter(category: ExpenseCategory?) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        loadExpenses()
    }
    
    fun updateGroupBy(groupBy: GroupBy) {
        _uiState.value = _uiState.value.copy(groupBy = groupBy)
        loadExpenses()
    }
    
    fun clearFilters() {
        _uiState.value = _uiState.value.copy(
            selectedStartDate = null,
            selectedEndDate = null,
            selectedCategory = null,
            groupBy = GroupBy.TIME
        )
        loadExpenses()
    }
    
    fun refresh() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        loadExpenses()
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

/**
 * UI State for the Expense List Screen
 */
data class ExpenseListUiState(
    val expenses: List<Expense> = emptyList(),
    val groupedExpenses: Map<Any, List<Expense>> = emptyMap(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val selectedStartDate: LocalDate? = null,
    val selectedEndDate: LocalDate? = null,
    val selectedCategory: ExpenseCategory? = null,
    val groupBy: GroupBy = GroupBy.TIME
) {
    val totalAmount: Double
        get() = expenses.sumOf { it.amount }
    
    val totalCount: Int
        get() = expenses.size
    
    val isEmpty: Boolean
        get() = expenses.isEmpty() && !isLoading
}

package com.example.expensetracker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.domain.model.ExpenseReport
import com.example.expensetracker.domain.model.ReportPeriod
import com.example.expensetracker.domain.usecase.GetExpenseReportUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * ViewModel for the Expense Report Screen
 * Manages the state and business logic for expense reports and analytics
 */
@HiltViewModel
class ExpenseReportViewModel @Inject constructor(
    private val getExpenseReportUseCase: GetExpenseReportUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ExpenseReportUiState())
    val uiState: StateFlow<ExpenseReportUiState> = _uiState.asStateFlow()
    
    init {
        loadReport(ReportPeriod.LAST_7_DAYS)
    }
    
    fun loadReport(period: ReportPeriod) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        
        viewModelScope.launch {
            try {
                val report = getExpenseReportUseCase.getReport(period)
                _uiState.value = _uiState.value.copy(
                    report = report,
                    selectedPeriod = period,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load report"
                )
            }
        }
    }
    
    fun loadCustomReport(startDate: LocalDate, endDate: LocalDate) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        
        viewModelScope.launch {
            try {
                val report = getExpenseReportUseCase.getCustomReport(startDate, endDate)
                _uiState.value = _uiState.value.copy(
                    report = report,
                    selectedPeriod = ReportPeriod.CUSTOM,
                    customStartDate = startDate,
                    customEndDate = endDate,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load custom report"
                )
            }
        }
    }
    
    fun refresh() {
        val currentState = _uiState.value
        if (currentState.selectedPeriod == ReportPeriod.CUSTOM) {
            currentState.customStartDate?.let { startDate ->
                currentState.customEndDate?.let { endDate ->
                    loadCustomReport(startDate, endDate)
                }
            }
        } else {
            loadReport(currentState.selectedPeriod)
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

/**
 * UI State for the Expense Report Screen
 */
data class ExpenseReportUiState(
    val report: ExpenseReport? = null,
    val selectedPeriod: ReportPeriod = ReportPeriod.LAST_7_DAYS,
    val customStartDate: LocalDate? = null,
    val customEndDate: LocalDate? = null,
    val isLoading: Boolean = true,
    val error: String? = null
) {
    val isEmpty: Boolean
        get() = report?.totalCount == 0 && !isLoading
}

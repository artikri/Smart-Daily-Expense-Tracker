package com.example.expensetracker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing theme preferences
 */
@HiltViewModel
class ThemeViewModel @Inject constructor() : ViewModel() {
    
    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()
    
    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }
    
    fun setDarkTheme(isDark: Boolean) {
        _isDarkTheme.value = isDark
    }
}

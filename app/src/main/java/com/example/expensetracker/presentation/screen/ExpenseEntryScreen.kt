package com.example.expensetracker.presentation.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.expensetracker.domain.model.ExpenseCategory
import com.example.expensetracker.presentation.component.*
import com.example.expensetracker.presentation.viewmodel.ExpenseEntryViewModel

/**
 * Expense Entry Screen
 * Allows users to add new expenses with validation and real-time feedback
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseEntryScreen(
    onNavigateToList: () -> Unit,
    onNavigateToReport: () -> Unit,
    themeViewModel: com.example.expensetracker.presentation.viewmodel.ThemeViewModel,
    viewModel: ExpenseEntryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Handle success state
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            // Auto-clear success message after 3 seconds
            kotlinx.coroutines.delay(3000)
            viewModel.clearSuccess()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Expense") },
                actions = {
                    IconButton(onClick = { themeViewModel.toggleTheme() }) {
                        Icon(
                            if (themeViewModel.isDarkTheme.value) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Theme"
                        )
                    }
                    IconButton(onClick = onNavigateToList) {
                        Icon(Icons.Default.List, contentDescription = "View Expenses")
                    }
                    IconButton(onClick = onNavigateToReport) {
                        Icon(Icons.Default.Analytics, contentDescription = "View Reports")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Today's Summary Card
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically() + fadeIn()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Total Spent Today",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        AmountDisplay(
                            amount = uiState.todayTotalAmount,
                            style = AmountDisplayStyle.LARGE
                        )
                        Text(
                            text = "${uiState.todayExpenseCount} expenses",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            
            // Success Message
            AnimatedVisibility(
                visible = uiState.isSuccess,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                uiState.successMessage?.let { message ->
                    SuccessMessage(message = message)
                }
            }
            
            // Error Message
            AnimatedVisibility(
                visible = uiState.error != null,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                uiState.error?.let { error ->
                    ErrorMessage(
                        message = error,
                        onRetry = { viewModel.clearError() }
                    )
                }
            }
            
            // Title Input
            OutlinedTextField(
                value = uiState.title,
                onValueChange = viewModel::updateTitle,
                label = { Text("Title *") },
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.titleError != null,
                supportingText = uiState.titleError?.let { { Text(it) } }
            )
            
            // Amount Input
            OutlinedTextField(
                value = uiState.amount,
                onValueChange = viewModel::updateAmount,
                label = { Text("Amount (₹) *") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = uiState.amountError != null,
                supportingText = uiState.amountError?.let { { Text(it) } }
            )
            
            // Category Selection
            Text(
                text = "Category",
                style = MaterialTheme.typography.titleMedium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ExpenseCategory.values().forEach { category ->
                    CategoryChip(
                        category = category,
                        isSelected = uiState.category == category,
                        onClick = { viewModel.updateCategory(category) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Notes Input
            OutlinedTextField(
                value = uiState.notes,
                onValueChange = viewModel::updateNotes,
                label = { Text("Notes (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 3,
                placeholder = { Text("Add any additional notes...") }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Add Expense Button
            Button(
                onClick = viewModel::addExpense,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Add Expense")
            }
        }
    }
}

package com.example.expensetracker.presentation.screen

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.expensetracker.domain.model.Expense
import com.example.expensetracker.domain.model.ExpenseCategory
import com.example.expensetracker.domain.model.GroupBy
import com.example.expensetracker.presentation.component.*
import com.example.expensetracker.presentation.viewmodel.ExpenseListViewModel
import java.time.format.DateTimeFormatter

/**
 * Expense List Screen
 * Displays expenses with filtering and grouping options
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToReport: () -> Unit,
    themeViewModel: com.example.expensetracker.presentation.viewmodel.ThemeViewModel,
    viewModel: ExpenseListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showFilterDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Expenses") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { themeViewModel.toggleTheme() }) {
                        Icon(
                            if (themeViewModel.isDarkTheme.value) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Theme"
                        )
                    }
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    }
                    IconButton(onClick = onNavigateToReport) {
                        Icon(Icons.Default.Analytics, contentDescription = "View Reports")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateBack
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Expense")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Summary Cards
            if (!uiState.isEmpty) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        title = "Total",
                        value = "₹${String.format("%.2f", uiState.totalAmount)}",
                        icon = {
                            Icon(
                                Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Count",
                        value = uiState.totalCount.toString(),
                        icon = {
                            Icon(
                                Icons.Default.Receipt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Group By Toggle
            if (!uiState.isEmpty) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        onClick = { viewModel.updateGroupBy(GroupBy.TIME) },
                        label = { Text("By Time") },
                        selected = uiState.groupBy == GroupBy.TIME
                    )
                    FilterChip(
                        onClick = { viewModel.updateGroupBy(GroupBy.CATEGORY) },
                        label = { Text("By Category") },
                        selected = uiState.groupBy == GroupBy.CATEGORY
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Content
            when {
                uiState.isLoading -> {
                    LoadingIndicator()
                }
                uiState.error != null -> {
                    val errorMessage = uiState.error ?: ""
                    ErrorMessage(
                        message = errorMessage,
                        onRetry = { viewModel.refresh() }
                    )
                }
                uiState.isEmpty -> {
                    EmptyState(
                        title = "No expenses found",
                        subtitle = "Start by adding your first expense"
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        uiState.groupedExpenses.forEach { (group, expenses) ->
                            item {
                                GroupHeader(
                                    group = group,
                                    groupBy = uiState.groupBy,
                                    expenseCount = expenses.size,
                                    totalAmount = expenses.sumOf { it.amount }
                                )
                            }
                            items(expenses) { expense ->
                                ExpenseItem(expense = expense)
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Filter Dialog
    if (showFilterDialog) {
        FilterDialog(
            onDismiss = { showFilterDialog = false },
            onApplyFilters = { startDate, endDate, category ->
                viewModel.updateDateFilter(startDate, endDate)
                viewModel.updateCategoryFilter(category)
                showFilterDialog = false
            },
            onClearFilters = {
                viewModel.clearFilters()
                showFilterDialog = false
            }
        )
    }
}

@Composable
private fun GroupHeader(
    group: Any,
    groupBy: GroupBy,
    expenseCount: Int,
    totalAmount: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = when (groupBy) {
                        GroupBy.TIME -> {
                            val date = group as java.time.LocalDate
                            date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
                        }
                        GroupBy.CATEGORY -> {
                            val category = group as ExpenseCategory
                            "${category.icon} ${category.displayName}"
                        }
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$expenseCount expenses",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            AmountDisplay(
                amount = totalAmount,
                style = AmountDisplayStyle.MEDIUM
            )
        }
    }
}

@Composable
private fun ExpenseItem(
    expense: Expense
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = expense.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${expense.category.icon} ${expense.category.displayName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                expense.notes?.let { notes ->
                    if (notes.isNotBlank()) {
                        Text(
                            text = notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Column(
                horizontalAlignment = Alignment.End
            ) {
                AmountDisplay(
                    amount = expense.amount,
                    style = AmountDisplayStyle.SMALL
                )
                Text(
                    text = expense.createdAt.format(DateTimeFormatter.ofPattern("HH:mm")),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun FilterDialog(
    onDismiss: () -> Unit,
    onApplyFilters: (startDate: java.time.LocalDate?, endDate: java.time.LocalDate?, category: ExpenseCategory?) -> Unit,
    onClearFilters: () -> Unit
) {
    var startDate by remember { mutableStateOf<java.time.LocalDate?>(null) }
    var endDate by remember { mutableStateOf<java.time.LocalDate?>(null) }
    var selectedCategory by remember { mutableStateOf<ExpenseCategory?>(null) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter Expenses") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Category")
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExpenseCategory.values().forEach { category ->
                        CategoryChip(
                            category = category,
                            isSelected = selectedCategory == category,
                            onClick = { 
                                selectedCategory = if (selectedCategory == category) null else category
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onApplyFilters(startDate, endDate, selectedCategory)
                }
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onClearFilters) {
                Text("Clear All")
            }
        }
    )
}

package com.example.expensetracker.presentation.screen

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.expensetracker.domain.model.ReportPeriod
import com.example.expensetracker.presentation.component.*
import com.example.expensetracker.presentation.viewmodel.ExpenseReportViewModel
import java.time.format.DateTimeFormatter

/**
 * Expense Report Screen
 * Displays expense analytics with charts and summaries
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseReportScreen(
    onNavigateBack: () -> Unit,
    themeViewModel: com.example.expensetracker.presentation.viewmodel.ThemeViewModel,
    viewModel: ExpenseReportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Expense Report") },
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
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Period Selection
            PeriodSelectionChips(
                selectedPeriod = uiState.selectedPeriod,
                onPeriodSelected = viewModel::loadReport
            )
            
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
                        title = "No data available",
                        subtitle = "No expenses found for the selected period"
                    )
                }
                uiState.report != null -> {
                    uiState.report?.let { report ->
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                ReportSummaryCard(report = report)
                            }
                            
                            item {
                                CategoryBreakdownCard(report = report)
                            }
                            
                            item {
                                DailyTrendsCard(report = report)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PeriodSelectionChips(
    selectedPeriod: ReportPeriod,
    onPeriodSelected: (ReportPeriod) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val periods = ReportPeriod.values().filter { it != ReportPeriod.CUSTOM }
        items(periods) { period ->
            FilterChip(
                onClick = { onPeriodSelected(period) },
                label = { Text(period.name.replace("_", " ")) },
                selected = selectedPeriod == period
            )
        }
    }
}

@Composable
private fun ReportSummaryCard(
    report: com.example.expensetracker.domain.model.ExpenseReport
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Summary",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatCard(
                    title = "Total Amount",
                    value = "₹${String.format("%.2f", report.totalAmount)}",
                    icon = {
                        Icon(
                            Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                )
                StatCard(
                    title = "Total Expenses",
                    value = report.totalCount.toString(),
                    icon = {
                        Icon(
                            Icons.Default.Receipt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun CategoryBreakdownCard(
    report: com.example.expensetracker.domain.model.ExpenseReport
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Category Breakdown",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            report.categoryTotals.forEach { categoryTotal ->
                CategoryBreakdownItem(categoryTotal = categoryTotal)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun CategoryBreakdownItem(
    categoryTotal: com.example.expensetracker.domain.model.CategoryTotal
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = categoryTotal.category.icon,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = categoryTotal.category.displayName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${categoryTotal.count} expenses",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Column(
            horizontalAlignment = Alignment.End
        ) {
            AmountDisplay(
                amount = categoryTotal.amount,
                style = AmountDisplayStyle.SMALL
            )
            Text(
                text = "${String.format("%.1f", categoryTotal.percentage)}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DailyTrendsCard(
    report: com.example.expensetracker.domain.model.ExpenseReport
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Daily Trends",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            // Simple bar chart representation
            report.dailyTotals.forEach { dailyTotal ->
                DailyTrendItem(dailyTotal = dailyTotal, maxAmount = report.dailyTotals.maxOfOrNull { it.amount } ?: 1.0)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun DailyTrendItem(
    dailyTotal: com.example.expensetracker.domain.model.DailyTotal,
    maxAmount: Double
) {
    val progress = if (maxAmount > 0) (dailyTotal.amount / maxAmount).toFloat() else 0f
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = dailyTotal.date.format(DateTimeFormatter.ofPattern("MMM dd")),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            AmountDisplay(
                amount = dailyTotal.amount,
                style = AmountDisplayStyle.SMALL
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

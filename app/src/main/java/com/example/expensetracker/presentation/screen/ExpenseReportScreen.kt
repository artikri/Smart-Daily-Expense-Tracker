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
import androidx.compose.ui.platform.LocalContext
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
    val context = LocalContext.current
    
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
            var showStartPicker by remember { mutableStateOf(false) }
            var showEndPicker by remember { mutableStateOf(false) }
            var customStart by remember { mutableStateOf(java.time.LocalDate.now().minusDays(6)) }
            var customEnd by remember { mutableStateOf(java.time.LocalDate.now()) }

            PeriodSelectionChips(
                selectedPeriod = uiState.selectedPeriod,
                onPeriodSelected = viewModel::loadReport
            )

            if (uiState.selectedPeriod == com.example.expensetracker.domain.model.ReportPeriod.CUSTOM) {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(onClick = { showStartPicker = true }) { Text("Start: ${customStart}") }
                    OutlinedButton(onClick = { showEndPicker = true }) { Text("End: ${customEnd}") }
                    Button(onClick = { viewModel.loadCustomReport(customStart, customEnd) }) { Text("Apply") }
                }
            }

            if (showStartPicker) {
                DatePickerDialog(onDismissRequest = { showStartPicker = false }, confirmButton = {
                    TextButton(onClick = {
                        showStartPicker = false
                    }) { Text("OK") }
                }) {
                    val state = rememberDatePickerState()
                    DatePicker(state = state)
                    LaunchedEffect(state.selectedDateMillis) {
                        state.selectedDateMillis?.let {
                            customStart = java.time.Instant.ofEpochMilli(it).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                        }
                    }
                }
            }
            if (showEndPicker) {
                DatePickerDialog(onDismissRequest = { showEndPicker = false }, confirmButton = {
                    TextButton(onClick = {
                        showEndPicker = false
                    }) { Text("OK") }
                }) {
                    val state = rememberDatePickerState()
                    DatePicker(state = state)
                    LaunchedEffect(state.selectedDateMillis) {
                        state.selectedDateMillis?.let {
                            customEnd = java.time.Instant.ofEpochMilli(it).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                        }
                    }
                }
            }

            // Export buttons - only show when report is available
            if (uiState.report != null && !uiState.isLoading) {
                ExportButtons(
                    onExportCsv = {
                        val shareIntent = viewModel.exportAsCsv(context)
                        shareIntent?.let { context.startActivity(it) }
                    },
                    onExportPdf = {
                        val shareIntent = viewModel.exportAsPdf(context)
                        shareIntent?.let { context.startActivity(it) }
                    }
                )
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
        val periods = listOf(ReportPeriod.TODAY, ReportPeriod.LAST_7_DAYS, ReportPeriod.CUSTOM)
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

            // Always show amount prominently
            AmountDisplay(amount = report.totalAmount, style = AmountDisplayStyle.LARGE)
            Text(
                text = "Total Amount",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Show total count as a simple line to avoid being hidden on small screens
            Text(
                text = "Total Expenses: ${report.totalCount}",
                style = MaterialTheme.typography.bodyLarge
            )
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

@Composable
private fun ExportButtons(
    onExportCsv: () -> Unit,
    onExportPdf: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Export Report",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onExportCsv,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.TableChart,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export CSV")
                }
                
                OutlinedButton(
                    onClick = onExportPdf,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.PictureAsPdf,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export PDF")
                }
            }
            
            Text(
                text = "Files will be shared via your preferred app",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

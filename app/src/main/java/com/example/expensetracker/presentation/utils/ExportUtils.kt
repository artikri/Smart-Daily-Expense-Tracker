package com.example.expensetracker.presentation.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.expensetracker.domain.model.ExpenseReport
import com.example.expensetracker.domain.model.ReportPeriod
import java.io.File
import java.io.FileWriter
import java.time.format.DateTimeFormatter

/**
 * Utility class for exporting expense reports
 * Simulates PDF/CSV export functionality
 */
object ExportUtils {
    
    /**
     * Export report as CSV format
     */
    fun exportAsCsv(context: Context, report: ExpenseReport, period: ReportPeriod): Uri? {
        return try {
            val fileName = "expense_report_${getPeriodString(period)}.csv"
            val file = File(context.cacheDir, fileName)
            
            FileWriter(file).use { writer ->
                // Write CSV header
                writer.appendLine("Expense Report - ${getPeriodString(period)}")
                writer.appendLine("Generated on: ${java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}")
                writer.appendLine()
                
                // Summary section
                writer.appendLine("SUMMARY")
                writer.appendLine("Total Amount,${report.totalAmount}")
                writer.appendLine("Total Expenses,${report.totalCount}")
                writer.appendLine()
                
                // Category breakdown
                writer.appendLine("CATEGORY BREAKDOWN")
                writer.appendLine("Category,Amount,Count,Percentage")
                report.categoryTotals.forEach { categoryTotal ->
                    writer.appendLine("${categoryTotal.category.displayName},${categoryTotal.amount},${categoryTotal.count},${String.format("%.1f", categoryTotal.percentage)}%")
                }
                writer.appendLine()
                
                // Daily trends
                writer.appendLine("DAILY TRENDS")
                writer.appendLine("Date,Amount")
                report.dailyTotals.forEach { dailyTotal ->
                    writer.appendLine("${dailyTotal.date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))},${dailyTotal.amount}")
                }
            }
            
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Export report as PDF format (simulated)
     * Creates a text file with PDF-like formatting
     */
    fun exportAsPdf(context: Context, report: ExpenseReport, period: ReportPeriod): Uri? {
        return try {
            val fileName = "expense_report_${getPeriodString(period)}.txt"
            val file = File(context.cacheDir, fileName)
            
            FileWriter(file).use { writer ->
                // PDF-like header
                writer.appendLine("=" * 50)
                writer.appendLine("EXPENSE REPORT")
                writer.appendLine("Period: ${getPeriodString(period)}")
                writer.appendLine("Generated: ${java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}")
                writer.appendLine("=" * 50)
                writer.appendLine()
                
                // Summary section
                writer.appendLine("SUMMARY")
                writer.appendLine("-" * 20)
                writer.appendLine("Total Amount: $${String.format("%.2f", report.totalAmount)}")
                writer.appendLine("Total Expenses: ${report.totalCount}")
                writer.appendLine()
                
                // Category breakdown
                writer.appendLine("CATEGORY BREAKDOWN")
                writer.appendLine("-" * 20)
                report.categoryTotals.forEach { categoryTotal ->
                    writer.appendLine("${categoryTotal.category.icon} ${categoryTotal.category.displayName}")
                    writer.appendLine("  Amount: $${String.format("%.2f", categoryTotal.amount)}")
                    writer.appendLine("  Count: ${categoryTotal.count}")
                    writer.appendLine("  Percentage: ${String.format("%.1f", categoryTotal.percentage)}%")
                    writer.appendLine()
                }
                
                // Daily trends
                writer.appendLine("DAILY TRENDS")
                writer.appendLine("-" * 20)
                report.dailyTotals.forEach { dailyTotal ->
                    writer.appendLine("${dailyTotal.date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))}: $${String.format("%.2f", dailyTotal.amount)}")
                }
                writer.appendLine()
                writer.appendLine("=" * 50)
            }
            
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Create share intent for the exported file
     */
    fun createShareIntent(context: Context, uri: Uri, mimeType: String): Intent {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Expense Report")
            putExtra(Intent.EXTRA_TEXT, "Please find attached the expense report.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        return Intent.createChooser(shareIntent, "Share Expense Report")
    }
    
    /**
     * Get period string for file naming
     */
    private fun getPeriodString(period: ReportPeriod): String {
        return when (period) {
            ReportPeriod.TODAY -> "today"
            ReportPeriod.LAST_7_DAYS -> "last_7_days"
            ReportPeriod.LAST_30_DAYS -> "last_30_days"
            ReportPeriod.CUSTOM -> "custom_period"
        }
    }
    
    /**
     * Extension function to repeat string
     */
    private operator fun String.times(count: Int): String {
        return this.repeat(count)
    }
}

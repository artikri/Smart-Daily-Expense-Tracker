package com.example.expensetracker.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.expensetracker.presentation.screen.ExpenseEntryScreen
import com.example.expensetracker.presentation.screen.ExpenseListScreen
import com.example.expensetracker.presentation.screen.ExpenseReportScreen

/**
 * Navigation routes for the Expense Tracker app
 */
object ExpenseTrackerRoutes {
    const val EXPENSE_ENTRY = "expense_entry"
    const val EXPENSE_LIST = "expense_list"
    const val EXPENSE_REPORT = "expense_report"
}

/**
 * Navigation composable for the Expense Tracker app
 * Manages screen transitions and routing
 */
@Composable
fun ExpenseTrackerNavigation(
    navController: NavHostController,
    themeViewModel: com.example.expensetracker.presentation.viewmodel.ThemeViewModel,
    startDestination: String = ExpenseTrackerRoutes.EXPENSE_ENTRY
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(ExpenseTrackerRoutes.EXPENSE_ENTRY) {
            ExpenseEntryScreen(
                onNavigateToList = {
                    navController.navigate(ExpenseTrackerRoutes.EXPENSE_LIST)
                },
                onNavigateToReport = {
                    navController.navigate(ExpenseTrackerRoutes.EXPENSE_REPORT)
                },
                themeViewModel = themeViewModel
            )
        }
        
        composable(ExpenseTrackerRoutes.EXPENSE_LIST) {
            ExpenseListScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToReport = {
                    navController.navigate(ExpenseTrackerRoutes.EXPENSE_REPORT)
                },
                themeViewModel = themeViewModel
            )
        }
        
        composable(ExpenseTrackerRoutes.EXPENSE_REPORT) {
            ExpenseReportScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                themeViewModel = themeViewModel
            )
        }
    }
}

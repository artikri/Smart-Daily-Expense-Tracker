package com.example.expensetracker

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for Expense Tracker
 * Initializes Hilt dependency injection
 */
@HiltAndroidApp
class ExpenseTrackerApplication : Application()

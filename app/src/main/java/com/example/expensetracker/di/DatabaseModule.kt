package com.example.expensetracker.di

import android.content.Context
import com.example.expensetracker.data.local.ExpenseDatabase
import com.example.expensetracker.data.local.dao.ExpenseDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for database dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideExpenseDatabase(@ApplicationContext context: Context): ExpenseDatabase {
        return ExpenseDatabase.getDatabase(context)
    }
    
    @Provides
    fun provideExpenseDao(database: ExpenseDatabase): ExpenseDao {
        return database.expenseDao()
    }
}

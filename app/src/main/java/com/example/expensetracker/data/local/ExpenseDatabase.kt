package com.example.expensetracker.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.example.expensetracker.data.local.dao.ExpenseDao
import com.example.expensetracker.data.local.entity.ExpenseEntity

/**
 * Room database for the Expense Tracker app
 * Manages all local data storage
 */
@Database(
    entities = [ExpenseEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ExpenseDatabase : RoomDatabase() {
    
    abstract fun expenseDao(): ExpenseDao
    
    companion object {
        @Volatile
        private var INSTANCE: ExpenseDatabase? = null
        
        fun getDatabase(context: Context): ExpenseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ExpenseDatabase::class.java,
                    "expense_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

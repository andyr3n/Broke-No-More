package com.example.broke_no_more.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Expense::class], version = 2, exportSchema = false)
abstract class ExpenseDatabase : RoomDatabase() {
    abstract val expenseDatabaseDao: ExpenseDatabaseDao

    companion object {
        @Volatile
        private var INSTANCE: ExpenseDatabase? = null

        fun getInstance(context: Context): ExpenseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ExpenseDatabase::class.java,
                    "expense_database"
                )
                    .fallbackToDestructiveMigration() // For development purposes only
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}



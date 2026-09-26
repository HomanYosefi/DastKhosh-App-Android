package com.homan.dastkhosh.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.homan.dastkhosh.data.local.dao.ExpenseDao
import com.homan.dastkhosh.data.local.entity.ExpenseEntity

@Database(entities = [ExpenseEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
}
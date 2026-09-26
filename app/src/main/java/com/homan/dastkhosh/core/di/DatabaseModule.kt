package com.homan.dastkhosh.core.di

import android.content.Context
import androidx.room.Room
import com.homan.dastkhosh.data.local.dao.ExpenseDao
import com.homan.dastkhosh.data.local.database.AppDatabase
import com.homan.dastkhosh.data.repository.ExpenseRepositoryImpl
import com.homan.dastkhosh.domain.repository.ExpenseRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {


    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "dastkhosh_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideExpenseDao(
        database: AppDatabase
    ): ExpenseDao {
        return database.expenseDao()
    }


    @Provides
    @Singleton
    fun provideExpenseRepository(
        expenseDao: ExpenseDao
    ): ExpenseRepository {
        return ExpenseRepositoryImpl(expenseDao)
    }
}
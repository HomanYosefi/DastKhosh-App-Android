package com.homan.dastkhosh.domain.repository

import com.homan.dastkhosh.domain.models.Expense
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository{
    fun getAllExpenses(): Flow<List<Expense>>
    suspend fun insertExpense(expense: Expense)
    suspend fun deleteExpense(expense: Expense)

    suspend fun getAllExpensesSync(): List<Expense>

    suspend fun insertExpenses(expenses: List<Expense>)
}
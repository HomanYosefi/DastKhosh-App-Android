package com.homan.dastkhosh.data.repository

import com.homan.dastkhosh.data.local.dao.ExpenseDao
import com.homan.dastkhosh.data.mapper.toDomain
import com.homan.dastkhosh.data.mapper.toEntity
import com.homan.dastkhosh.domain.models.Expense
import com.homan.dastkhosh.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map



class ExpenseRepositoryImpl(private val dao: ExpenseDao) : ExpenseRepository {
    override fun getAllExpenses(): Flow<List<Expense>> =
        dao.getAllExpenses().map { list -> list.map { it.toDomain() } }



    override suspend fun insertExpense(expense: Expense) =
        dao.insertExpense(expense.toEntity())

    override suspend fun deleteExpense(expense: Expense) =
        dao.deleteExpense(expense.toEntity())

    override suspend fun getAllExpensesSync(): List<Expense> =
        dao.getAllExpensesSync().map { it.toDomain() }

    override suspend fun insertExpenses(expenses: List<Expense>) =
        dao.insertExpenses(expenses.map { it.toEntity() })
}
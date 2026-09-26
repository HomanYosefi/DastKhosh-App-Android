package com.homan.dastkhosh.domain.usecase

import com.homan.dastkhosh.domain.models.Expense
import com.homan.dastkhosh.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllExpensesUseCase @Inject constructor(
    private val repository: ExpenseRepository
) {
    operator fun invoke(): Flow<List<Expense>> {
        return repository.getAllExpenses()
    }
}
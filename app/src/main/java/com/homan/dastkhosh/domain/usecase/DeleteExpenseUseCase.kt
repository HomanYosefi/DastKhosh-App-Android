package com.homan.dastkhosh.domain.usecase

import com.homan.dastkhosh.domain.models.Expense
import com.homan.dastkhosh.domain.repository.ExpenseRepository
import javax.inject.Inject

class DeleteExpenseUseCase @Inject constructor(
    private val repository: ExpenseRepository
) {
    suspend operator fun invoke(expense: Expense) {
        repository.deleteExpense(expense)
    }
}
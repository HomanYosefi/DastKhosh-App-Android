package com.homan.dastkhosh.domain.usecase

import com.homan.dastkhosh.domain.models.Expense
import com.homan.dastkhosh.domain.repository.ExpenseRepository
import javax.inject.Inject

class InsertExpenseUseCase @Inject constructor(
    private val repository: ExpenseRepository
) {
    suspend operator fun invoke(
        amount: Long,
        description: String
    ) {
        repository.insertExpense(
            Expense(
                amount = amount,
                description = description
            )
        )
    }
}
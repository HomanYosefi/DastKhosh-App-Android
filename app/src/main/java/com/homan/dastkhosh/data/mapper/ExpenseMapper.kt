package com.homan.dastkhosh.data.mapper

import com.homan.dastkhosh.data.local.entity.ExpenseEntity
import com.homan.dastkhosh.domain.models.Expense

fun ExpenseEntity.toDomain(): Expense = Expense(
    id = id,
    amount = amount,
    description = description,
    createdAt = createdAt
)

fun Expense.toEntity(): ExpenseEntity = ExpenseEntity(
    id = id,
    amount = amount,
    description = description,
    createdAt = createdAt
)


package com.homan.dastkhosh.domain.models

data class Expense(
    val id: Int = 0,
    val amount: Long,
    val description: String,
    val createdAt: Long = System.currentTimeMillis()
)
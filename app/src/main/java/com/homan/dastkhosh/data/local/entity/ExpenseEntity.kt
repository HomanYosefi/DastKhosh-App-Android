package com.homan.dastkhosh.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val amount: Long,
    val description: String,
    val createdAt: Long
)



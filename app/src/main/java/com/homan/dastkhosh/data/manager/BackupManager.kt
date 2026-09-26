package com.homan.dastkhosh.data.manager


import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.homan.dastkhosh.domain.models.Expense
import com.homan.dastkhosh.domain.repository.ExpenseRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: ExpenseRepository
) {
    private val gson = Gson()

    suspend fun exportBackup(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val expenses = repository.getAllExpensesSync()
            val json = gson.toJson(expenses)

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(json.toByteArray())
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun importBackup(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val json = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader().use { it.readText() }
            }

            if (json != null) {
                val type = object : TypeToken<List<Expense>>() {}.type
                val expenses: List<Expense> = gson.fromJson(json, type)

                repository.insertExpenses(expenses)
                Result.success(Unit)
            } else {
                Result.failure(Exception("فایل خوانده نشد"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
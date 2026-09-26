package com.homan.dastkhosh.peresantation.analytics


import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homan.dastkhosh.domain.models.Expense
import com.homan.dastkhosh.domain.usecase.GetAllExpensesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

enum class TimeFilter(val title: String) {
    DAILY("روزانه"),
    WEEKLY("هفتگی"),
    MONTHLY("ماهانه"),
    YEARLY("سالانه")
}

data class ChartData(
    val label: String,
    val amount: Long,
    val percentage: Float,
    val color: Color
)

data class AnalyticsState(
    val timeFilter: TimeFilter = TimeFilter.MONTHLY,
    val totalAmount: Long = 0L,
    val chartData: List<ChartData> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val getAllExpensesUseCase: GetAllExpensesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AnalyticsState())
    val state = _state.asStateFlow()

    private val allExpenses = getAllExpensesUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val chartColors = listOf(
        Color(0xFF6C63FF), Color(0xFFFF6584), Color(0xFF3F3D56),
        Color(0xFF00BFA6), Color(0xFFFFA000), Color(0xFFE05263)
    )

    init {
        viewModelScope.launch {
            combine(allExpenses, _state.map { it.timeFilter }.distinctUntilChanged()) { expenses, filter ->
                processExpenses(expenses, filter)
            }.collect { newState ->
                _state.update {
                    it.copy(
                        totalAmount = newState.totalAmount,
                        chartData = newState.chartData,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun setTimeFilter(filter: TimeFilter) {
        _state.update { it.copy(timeFilter = filter, isLoading = true) }
    }


    private fun processExpenses(expenses: List<Expense>, filter: TimeFilter): AnalyticsState {
        val filteredExpenses = filterByTime(expenses, filter)

        val total = filteredExpenses.sumOf { it.amount }
        if (total == 0L) return AnalyticsState(filter, 0L, emptyList(), false)

        val grouped = groupDynamically(filteredExpenses)

        val chartDataList = grouped.entries
            .sortedByDescending { it.value }
            .mapIndexed { index, entry ->
                ChartData(
                    label = entry.key,
                    amount = entry.value,
                    percentage = entry.value.toFloat() / total.toFloat(),
                    color = chartColors[index % chartColors.size]
                )
            }

        return AnalyticsState(
            timeFilter = filter,
            totalAmount = total,
            chartData = chartDataList,
            isLoading = false
        )
    }

    private fun groupDynamically(expenses: List<Expense>): Map<String, Long> {
        val groups = mutableMapOf<String, Long>()

        for (expense in expenses) {
            val normalizedDesc = expense.description.trim()
                .replace("\\s+".toRegex(), " ")
                .replace("ي", "ی")
                .replace("ك", "ک")

            var finalKey = normalizedDesc
            var oldKeyToRemove: String? = null

            for (existingKey in groups.keys) {
                if (normalizedDesc.contains(existingKey)) {
                    finalKey = existingKey
                    break
                } else if (existingKey.contains(normalizedDesc)) {
                    finalKey = normalizedDesc
                    oldKeyToRemove = existingKey
                    break
                }
            }

            if (oldKeyToRemove != null) {
                val oldAmount = groups.remove(oldKeyToRemove) ?: 0L
                groups[finalKey] = oldAmount + expense.amount
            } else {
                groups[finalKey] = groups.getOrDefault(finalKey, 0L) + expense.amount
            }
        }
        return groups
    }

    private fun filterByTime(expenses: List<Expense>, filter: TimeFilter): List<Expense> {
        val nowCal = Calendar.getInstance()
        val expenseCal = Calendar.getInstance()

        return expenses.filter { expense ->
            expenseCal.timeInMillis = expense.createdAt // یا expense.timestamp

            when (filter) {
                TimeFilter.DAILY -> isSameDay(nowCal, expenseCal)
                TimeFilter.WEEKLY -> isSameWeek(nowCal, expenseCal)
                TimeFilter.MONTHLY -> isSameMonth(nowCal, expenseCal)
                TimeFilter.YEARLY -> isSameYear(nowCal, expenseCal)
            }
        }
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar) =
        cal1[Calendar.YEAR] == cal2[Calendar.YEAR] && cal1[Calendar.DAY_OF_YEAR] == cal2[Calendar.DAY_OF_YEAR]

    private fun isSameWeek(cal1: Calendar, cal2: Calendar) =
        cal1[Calendar.YEAR] == cal2[Calendar.YEAR] && cal1[Calendar.WEEK_OF_YEAR] == cal2[Calendar.WEEK_OF_YEAR]

    private fun isSameMonth(cal1: Calendar, cal2: Calendar) =
        cal1[Calendar.YEAR] == cal2[Calendar.YEAR] && cal1[Calendar.MONTH] == cal2[Calendar.MONTH]

    private fun isSameYear(cal1: Calendar, cal2: Calendar) =
        cal1[Calendar.YEAR] == cal2[Calendar.YEAR]




    private fun isSameDay(time1: Long, time2: Long): Boolean {
        return true
    }
    private fun isSameWeek(time1: Long, time2: Long) = true
    private fun isSameMonth(time1: Long, time2: Long) = true
    private fun isSameYear(time1: Long, time2: Long) = true
}
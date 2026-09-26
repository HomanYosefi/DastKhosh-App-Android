package com.homan.dastkhosh.presentation.expense

import android.icu.util.Calendar as IcuCalendar
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homan.dastkhosh.core.util.HistoryDates
import com.homan.dastkhosh.core.util.digitsOnly
import com.homan.dastkhosh.core.util.historyPercentText
import com.homan.dastkhosh.core.util.toPersianDigits
import com.homan.dastkhosh.domain.models.Expense
import com.homan.dastkhosh.domain.usecase.DeleteExpenseUseCase
import com.homan.dastkhosh.domain.usecase.GetAllExpensesUseCase
import com.homan.dastkhosh.domain.usecase.InsertExpenseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject
import kotlin.math.abs


enum class TimeFilter(val title: String) {
    DAILY("امروز"),
    WEEKLY("این هفته"),
    MONTHLY("این ماه"),
    ALL("همه"),
    CUSTOM("سفارشی"),
    YEARLY("امسال")
}

data class ChartData(
    val label: String,
    val amount: Long,
    val percentage: Float,
    val color: Color
)

data class CustomDateRange(
    val start: Long,
    val endInclusive: Long
)

data class FinancialSummary(
    val currentLabel: String = "",
    val previousLabel: String? = null,
    val currentAmount: Long = 0L,
    val previousAmount: Long? = null,
    val changePercent: Double? = null,
    val topCategory: String? = null,
    val topCategoryAmount: Long = 0L,
    val comparisonNote: String? = null
)

data class HistoryState(
    val filter: TimeFilter = TimeFilter.MONTHLY,
    val filteredExpenses: List<Expense> = emptyList(),
    val chartData: List<ChartData> = emptyList(),
    val totalAmount: Long = 0L,
    val searchQuery: String = "",
    val customRange: CustomDateRange? = null,
    val summary: FinancialSummary = FinancialSummary(),
    val insights: List<String> = emptyList()
)


data class ExpenseFormState(
    val amount: String = "",
    val description: String = "",
    val amountError: String? = null,
    val descriptionError: String? = null,
    val isSaving: Boolean = false,
    val errorTick: Int = 0,
    val savedTick: Int = 0
) {
    val amountValue: Long
        get() = amount.toLongOrNull() ?: 0L

    val isValid: Boolean
        get() = amountValue > 0L && description.isNotBlank()
}

sealed interface ExpenseEvent {
    data class Message(val text: String) : ExpenseEvent
    data class Deleted(val text: String) : ExpenseEvent
}


private data class HistorySelection(
    val filter: TimeFilter = TimeFilter.MONTHLY,
    val customRange: CustomDateRange? = null
)

private data class HistoryWindow(
    val start: Long,
    val endExclusive: Long
) {
    fun contains(timestamp: Long): Boolean {
        return timestamp >= start && timestamp < endExclusive
    }
}

private data class HistoryPeriods(
    val current: HistoryWindow?,
    val previous: HistoryWindow?,
    val currentLabel: String,
    val previousLabel: String? = null,
    val comparisonNote: String? = null
)


@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val insertExpenseUseCase: InsertExpenseUseCase,
    private val getAllExpensesUseCase: GetAllExpensesUseCase,
    private val deleteExpenseUseCase: DeleteExpenseUseCase
) : ViewModel() {

    private val chartColors = listOf(
        Color(0xFF6C63FF),
        Color(0xFFFF6584),
        Color(0xFF00BFA6),
        Color(0xFFFFA000),
        Color(0xFFE05263)
    )

    private val otherColor = Color(0xFF90A4AE)

    private val whitespaceRegex = Regex("\\s+")
    private val nonWordRegex = Regex("[^\\p{L}\\p{N}]+")

    private val categoryRules: List<Pair<String, Set<String>>> = listOf(
        "🚕 حمل‌ونقل" to setOf(
            "بنزین",
            "گازوئیل",
            "اسنپ",
            "تپسی",
            "تاکسی",
            "اتوبوس",
            "مترو",
            "کرایه",
            "پارکینگ"
        ),
        "🍔 غذا" to setOf(
            "غذا",
            "رستوران",
            "ناهار",
            "نهار",
            "شام",
            "صبحانه",
            "پیتزا",
            "برگر",
            "ساندویچ",
            "کافه",
            "قهوه"
        ),
        "🛒 خرید روزمره" to setOf(
            "سوپرمارکت",
            "سوپر",
            "خواربار",
            "میوه",
            "سبزی",
            "گوشت",
            "مرغ",
            "نان",
            "لبنیات"
        ),
        "💊 سلامت" to setOf(
            "دارو",
            "داروخانه",
            "پزشک",
            "دکتر",
            "درمان",
            "بیمارستان",
            "آزمایش",
            "دندانپزشکی"
        ),
        "🏠 خانه و قبوض" to setOf(
            "اجاره",
            "قبض",
            "شارژ",
            "برق",
            "گاز",
            "اینترنت"
        ),
        "👕 پوشاک" to setOf(
            "لباس",
            "کفش",
            "مانتو",
            "شلوار",
            "پیراهن"
        )
    )


    val expenses: StateFlow<List<Expense>> = getAllExpensesUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val total: StateFlow<Long> = expenses
        .map { expenseList ->
            expenseList.sumOf { it.amount }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0L
        )

    val topDescriptions: StateFlow<List<String>> = expenses
        .map { expenseList ->
            expenseList
                .map { expense ->
                    expense.description
                        .trim()
                        .replace(whitespaceRegex, " ")
                }
                .filter { it.isNotBlank() }
                .groupingBy { it }
                .eachCount()
                .entries
                .sortedByDescending { it.value }
                .take(5)
                .map { it.key }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )


    private val _historySelection = MutableStateFlow(
        HistorySelection()
    )

    private val _searchQuery = MutableStateFlow("")

    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val historyClock: Flow<Long> = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(60_000L)
        }
    }

    val historyState: StateFlow<HistoryState> = combine(
        expenses,
        _historySelection,
        _searchQuery,
        historyClock
    ) { expenseList, selection, query, now ->
        processHistory(
            expensesList = expenseList,
            selection = selection,
            query = query,
            now = now
        )
    }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HistoryState()
        )


    private val _formState = MutableStateFlow(ExpenseFormState())

    val formState: StateFlow<ExpenseFormState> = _formState.asStateFlow()

    private val _events = Channel<ExpenseEvent>(Channel.BUFFERED)

    val events: Flow<ExpenseEvent> = _events.receiveAsFlow()

    private var lastDeleted: Expense? = null


    fun setTimeFilter(filter: TimeFilter) {
        _historySelection.update { selection ->
            if (
                filter == TimeFilter.CUSTOM &&
                selection.customRange == null
            ) {
                selection
            } else {
                selection.copy(filter = filter)
            }
        }
    }

    fun onSearchQueryChange(value: String) {
        _searchQuery.value = value.take(120)
    }

    fun setCustomRange(start: Long, endInclusive: Long) {
        val normalizedStart = HistoryDates.startOfDay(start)
        val normalizedEnd = HistoryDates.startOfDay(endInclusive)
        val today = HistoryDates.startOfDay(
            System.currentTimeMillis()
        )

        if (
            normalizedStart > normalizedEnd ||
            normalizedEnd > today
        ) {
            return
        }

        _historySelection.value = HistorySelection(
            filter = TimeFilter.CUSTOM,
            customRange = CustomDateRange(
                start = normalizedStart,
                endInclusive = normalizedEnd
            )
        )
    }


    private fun normalizeHistoryText(value: String): String {
        return HistoryDates.englishDigits(value)
            .replace('ي', 'ی')
            .replace('ى', 'ی')
            .replace('ك', 'ک')
            .replace('\u200C', ' ')
            .replace(whitespaceRegex, " ")
            .trim()
            .lowercase(Locale.ROOT)
    }

    private fun detectCategory(description: String): String {
        val normalized = normalizeHistoryText(description)

        val words = normalized
            .replace(nonWordRegex, " ")
            .trim()
            .split(whitespaceRegex)
            .filter { it.isNotBlank() }
            .toSet()

        val matchedRule = categoryRules.firstOrNull { (_, keywords) ->
            words.any { word -> word in keywords }
        }

        return matchedRule?.first
            ?: normalized.ifBlank { "بدون توضیح" }
    }

    private fun groupDynamically(
        expensesList: List<Expense>
    ): Map<String, Long> {
        val groups = mutableMapOf<String, Long>()

        expensesList.forEach { expense ->
            val category = detectCategory(expense.description)

            groups[category] = (groups[category] ?: 0L) + expense.amount
        }

        return groups
    }


    private fun buildPeriods(
        selection: HistorySelection,
        now: Long
    ): HistoryPeriods {
        val today = HistoryDates.startOfDay(now)

        fun calendarPeriod(
            start: Long,
            field: Int,
            step: Int,
            currentLabel: String,
            previousLabel: String
        ): HistoryPeriods {
            val end = HistoryDates.shift(
                timestamp = start,
                field = field,
                amount = step
            )

            val previousStart = HistoryDates.shift(
                timestamp = start,
                field = field,
                amount = -step
            )

            return HistoryPeriods(
                current = HistoryWindow(
                    start = start,
                    endExclusive = end
                ),
                previous = HistoryWindow(
                    start = previousStart,
                    endExclusive = start
                ),
                currentLabel = currentLabel,
                previousLabel = previousLabel,
                comparisonNote = if (now < end) {
                    "این دوره هنوز کامل نشده؛ مقایسه با کل دورهٔ قبل است."
                } else {
                    null
                }
            )
        }

        return when (selection.filter) {
            TimeFilter.ALL -> {
                HistoryPeriods(
                    current = null,
                    previous = null,
                    currentLabel = "همهٔ هزینه‌ها"
                )
            }

            TimeFilter.DAILY -> {
                calendarPeriod(
                    start = today,
                    field = IcuCalendar.DAY_OF_MONTH,
                    step = 1,
                    currentLabel = "امروز",
                    previousLabel = "دیروز"
                )
            }

            TimeFilter.WEEKLY -> {
                val cal = HistoryDates.calendar(today)

                val daysSinceSaturday = (
                        cal[IcuCalendar.DAY_OF_WEEK] -
                                IcuCalendar.SATURDAY + 7
                        ) % 7

                val start = HistoryDates.addDays(
                    timestamp = today,
                    days = -daysSinceSaturday
                )

                calendarPeriod(
                    start = start,
                    field = IcuCalendar.DAY_OF_MONTH,
                    step = 7,
                    currentLabel = "این هفته",
                    previousLabel = "هفتهٔ قبل"
                )
            }

            TimeFilter.MONTHLY -> {
                val start = HistoryDates.calendar(today).apply {
                    set(IcuCalendar.DAY_OF_MONTH, 1)
                }.timeInMillis

                val previousStart = HistoryDates.shift(
                    timestamp = start,
                    field = IcuCalendar.MONTH,
                    amount = -1
                )

                calendarPeriod(
                    start = start,
                    field = IcuCalendar.MONTH,
                    step = 1,
                    currentLabel = HistoryDates.monthTitle(start),
                    previousLabel = HistoryDates.monthTitle(previousStart)
                )
            }

            TimeFilter.YEARLY -> {
                val start = HistoryDates.calendar(today).apply {
                    set(IcuCalendar.DAY_OF_MONTH, 1)
                    set(IcuCalendar.MONTH, 0)
                }.timeInMillis

                val previousStart = HistoryDates.shift(
                    timestamp = start,
                    field = IcuCalendar.YEAR,
                    amount = -1
                )

                calendarPeriod(
                    start = start,
                    field = IcuCalendar.YEAR,
                    step = 1,
                    currentLabel = HistoryDates.yearTitle(start),
                    previousLabel = HistoryDates.yearTitle(previousStart)
                )
            }

            TimeFilter.CUSTOM -> {
                val range = selection.customRange

                if (range == null) {
                    HistoryPeriods(
                        current = HistoryWindow(
                            start = 0L,
                            endExclusive = 0L
                        ),
                        previous = null,
                        currentLabel = "بازهٔ دلخواه"
                    )
                } else {
                    val endExclusive = HistoryDates.addDays(
                        timestamp = range.endInclusive,
                        days = 1
                    )

                    val dayCount = HistoryDates.inclusiveDayCount(
                        start = range.start,
                        endInclusive = range.endInclusive
                    )

                    val previousStart = HistoryDates.addDays(
                        timestamp = range.start,
                        days = -dayCount
                    )

                    val previousEndInclusive = HistoryDates.addDays(
                        timestamp = range.start,
                        days = -1
                    )

                    val currentLabel =
                        "${HistoryDates.format(range.start)} تا " +
                                HistoryDates.format(range.endInclusive)

                    val previousLabel =
                        "${HistoryDates.format(previousStart)} تا " +
                                HistoryDates.format(previousEndInclusive)

                    val daysText = dayCount.toString().toPersianDigits()

                    val incompleteNote = if (range.endInclusive == today) {
                        " روز پایانی بازه هنوز کامل نشده است."
                    } else {
                        ""
                    }

                    HistoryPeriods(
                        current = HistoryWindow(
                            start = range.start,
                            endExclusive = endExclusive
                        ),
                        previous = HistoryWindow(
                            start = previousStart,
                            endExclusive = range.start
                        ),
                        currentLabel = currentLabel,
                        previousLabel = previousLabel,
                        comparisonNote =
                            "مقایسه با بازهٔ $daysText روزهٔ " +
                                    "بلافاصله قبل از بازهٔ انتخابی." +
                                    incompleteNote
                    )
                }
            }
        }
    }


    private fun processHistory(
        expensesList: List<Expense>,
        selection: HistorySelection,
        query: String,
        now: Long
    ): HistoryState {
        val normalizedQuery = normalizeHistoryText(query)
        val periods = buildPeriods(selection, now)

        val matchingExpenses = expensesList.filter { expense ->
            expense.createdAt <= now &&
                    (
                            normalizedQuery.isBlank() ||
                                    normalizeHistoryText(expense.description)
                                        .contains(normalizedQuery)
                            )
        }

        val current = matchingExpenses
            .filter { expense ->
                periods.current?.contains(expense.createdAt) ?: true
            }
            .sortedByDescending { it.createdAt }

        val previous = periods.previous?.let { window ->
            matchingExpenses.filter { expense ->
                window.contains(expense.createdAt)
            }
        }.orEmpty()

        val currentTotal = current.sumOf { it.amount }

        val previousTotal: Long? = periods.previous?.let {
            previous.sumOf { expense -> expense.amount }
        }

        val currentGroups = groupDynamically(current)
        val previousGroups = groupDynamically(previous)

        val sortedGroups = currentGroups.entries.sortedWith(
            compareByDescending<Map.Entry<String, Long>> { it.value }
                .thenBy { it.key }
        )

        val chartData = buildList<ChartData> {
            if (currentTotal > 0L) {
                sortedGroups.take(5).forEachIndexed { index, entry ->
                    add(
                        ChartData(
                            label = entry.key,
                            amount = entry.value,
                            percentage = (
                                    entry.value.toDouble() /
                                            currentTotal.toDouble()
                                    ).toFloat(),
                            color = chartColors[index % chartColors.size]
                        )
                    )
                }

                val othersTotal = sortedGroups
                    .drop(5)
                    .sumOf { it.value }

                if (othersTotal > 0L) {
                    add(
                        ChartData(
                            label = "سایر دسته‌ها",
                            amount = othersTotal,
                            percentage = (
                                    othersTotal.toDouble() /
                                            currentTotal.toDouble()
                                    ).toFloat(),
                            color = otherColor
                        )
                    )
                }
            }
        }

        val topCategory = sortedGroups.firstOrNull()

        val changePercent: Double? = when {
            previousTotal == null -> null

            previousTotal > 0L -> {
                (
                        (currentTotal.toDouble() - previousTotal.toDouble()) /
                                previousTotal.toDouble()
                        ) * 100.0
            }

            currentTotal == 0L -> 0.0
            else -> null
        }

        val summary = FinancialSummary(
            currentLabel = periods.currentLabel,
            previousLabel = periods.previousLabel,
            currentAmount = currentTotal,
            previousAmount = previousTotal,
            changePercent = changePercent,
            topCategory = topCategory?.key,
            topCategoryAmount = topCategory?.value ?: 0L,
            comparisonNote = periods.comparisonNote
        )

        return HistoryState(
            filter = selection.filter,
            filteredExpenses = current,
            chartData = chartData,
            totalAmount = currentTotal,
            searchQuery = query,
            customRange = selection.customRange,
            summary = summary,
            insights = buildFinancialInsights(
                currentGroups = currentGroups,
                previousGroups = previousGroups,
                summary = summary,
                matchingExpenses = matchingExpenses,
                filter = selection.filter,
                now = now
            )
        )
    }


    private fun buildFinancialInsights(
        currentGroups: Map<String, Long>,
        previousGroups: Map<String, Long>,
        summary: FinancialSummary,
        matchingExpenses: List<Expense>,
        filter: TimeFilter,
        now: Long
    ): List<String> {
        return buildList {
            summary.topCategory?.let { category ->
                if (summary.currentAmount > 0L) {
                    val share = (
                            summary.topCategoryAmount.toDouble() /
                                    summary.currentAmount.toDouble()
                            ) * 100.0

                    add(
                        "بیشترین هزینهٔ ثبت‌شده در این بازه مربوط به " +
                                "«$category» بوده؛ " +
                                "${historyPercentText(share)}٪ از مجموع هزینه‌ها."
                    )
                }
            }

            if (summary.previousAmount != null) {
                val increasedCategory = currentGroups.entries
                    .filter { entry ->
                        val oldAmount = previousGroups[entry.key] ?: 0L

                        oldAmount > 0L && entry.value > oldAmount
                    }
                    .maxByOrNull { entry ->
                        entry.value - (previousGroups[entry.key] ?: 0L)
                    }

                if (increasedCategory != null) {
                    val oldAmount = previousGroups.getValue(
                        increasedCategory.key
                    )

                    val percent = (
                            (
                                    increasedCategory.value.toDouble() -
                                            oldAmount.toDouble()
                                    ) / oldAmount.toDouble()
                            ) * 100.0

                    add(
                        "هزینهٔ «${increasedCategory.key}» نسبت به دورهٔ قبل " +
                                "${historyPercentText(percent)}٪ افزایش داشته."
                    )
                } else {
                    summary.changePercent
                        ?.takeIf { it < 0.0 }
                        ?.let { percent ->
                            add(
                                "مجموع هزینه‌های ثبت‌شدهٔ این بازه نسبت به " +
                                        "دورهٔ قبل ${historyPercentText(percent)}٪ کمتر است."
                            )
                        }
                }
            }


            if (filter == TimeFilter.ALL) {
                val today = HistoryDates.startOfDay(now)

                val recentStart = HistoryDates.addDays(
                    timestamp = today,
                    days = -7
                )

                val baselineStart = HistoryDates.addDays(
                    timestamp = recentStart,
                    days = -28
                )

                val oldestExpenseTime = matchingExpenses
                    .minOfOrNull { it.createdAt }

                if (
                    oldestExpenseTime != null &&
                    oldestExpenseTime <= baselineStart
                ) {
                    val recentTotal = matchingExpenses
                        .filter { expense ->
                            expense.createdAt >= recentStart &&
                                    expense.createdAt < today
                        }
                        .sumOf { it.amount }

                    val baselineTotal = matchingExpenses
                        .filter { expense ->
                            expense.createdAt >= baselineStart &&
                                    expense.createdAt < recentStart
                        }
                        .sumOf { it.amount }

                    if (baselineTotal > 0L) {
                        val recentAverage = recentTotal.toDouble() / 7.0
                        val baselineAverage = baselineTotal.toDouble() / 28.0

                        val change = (
                                (recentAverage - baselineAverage) /
                                        baselineAverage
                                ) * 100.0

                        if (abs(change) >= 1.0) {
                            val direction = if (change < 0.0) {
                                "کمتر"
                            } else {
                                "بیشتر"
                            }

                            add(
                                "میانگین روزانهٔ هزینه‌های ثبت‌شده در ۷ روز کامل " +
                                        "گذشته، ${historyPercentText(change)}٪ $direction " +
                                        "از میانگین ۲۸ روز قبل از آن بوده."
                            )
                        }
                    }
                }
            }
        }.take(3)
    }


    fun onAmountChange(raw: String) {
        val clean = raw
            .digitsOnly()
            .trimStart('0')
            .take(12)

        _formState.update {
            it.copy(
                amount = clean,
                amountError = null
            )
        }
    }

    fun addQuickAmount(value: Long) {
        val next = (
                _formState.value.amountValue + value
                ).coerceIn(0L, 999_999_999_999L)

        _formState.update {
            it.copy(
                amount = if (next == 0L) "" else next.toString(),
                amountError = null
            )
        }
    }

    fun clearAmount() {
        _formState.update {
            it.copy(
                amount = "",
                amountError = null
            )
        }
    }

    fun onDescriptionChange(value: String) {
        _formState.update {
            it.copy(
                description = value.take(120),
                descriptionError = null
            )
        }
    }

    fun save() {
        val state = _formState.value

        if (state.isSaving) return

        val amountError = if (state.amountValue <= 0L) {
            "مبلغ را وارد کنید"
        } else {
            null
        }

        val descriptionError = if (state.description.isBlank()) {
            "توضیحات را وارد کنید"
        } else {
            null
        }

        if (amountError != null || descriptionError != null) {
            _formState.update {
                it.copy(
                    amountError = amountError,
                    descriptionError = descriptionError,
                    errorTick = it.errorTick + 1
                )
            }

            viewModelScope.launch {
                _events.send(
                    ExpenseEvent.Message("اطلاعات کامل نیست 🙏")
                )
            }

            return
        }

        _formState.update {
            it.copy(isSaving = true)
        }

        viewModelScope.launch {
            try {
                insertExpenseUseCase(
                    amount = state.amountValue,
                    description = state.description.trim()
                )

                _formState.update {
                    ExpenseFormState(
                        savedTick = it.savedTick + 1
                    )
                }

                _events.send(
                    ExpenseEvent.Message("هزینه با موفقیت ثبت شد ✅")
                )
            } catch (exception: kotlinx.coroutines.CancellationException) {
                throw exception
            } catch (_: Exception) {
                _formState.update {
                    it.copy(errorTick = it.errorTick + 1)
                }

                _events.send(
                    ExpenseEvent.Message(
                        "ثبت هزینه انجام نشد؛ دوباره تلاش کنید."
                    )
                )
            } finally {
                _formState.update {
                    it.copy(isSaving = false)
                }
            }
        }
    }


    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            try {
                deleteExpenseUseCase(expense)
                lastDeleted = expense

                _events.send(
                    ExpenseEvent.Deleted(
                        "«${expense.description}» حذف شد"
                    )
                )
            } catch (exception: kotlinx.coroutines.CancellationException) {
                throw exception
            } catch (_: Exception) {
                _events.send(
                    ExpenseEvent.Message(
                        "حذف هزینه انجام نشد؛ دوباره تلاش کنید."
                    )
                )
            }
        }
    }

    fun undoDelete() {
        val item = lastDeleted ?: return
        lastDeleted = null

        viewModelScope.launch {
            try {
                insertExpenseUseCase(
                    amount = item.amount,
                    description = item.description
                )
            } catch (exception: kotlinx.coroutines.CancellationException) {
                throw exception
            } catch (_: Exception) {
                if (lastDeleted == null) {
                    lastDeleted = item
                }

                _events.send(
                    ExpenseEvent.Message(
                        "بازگردانی هزینه انجام نشد؛ دوباره تلاش کنید."
                    )
                )
            }
        }
    }

    fun consumeSavedTick() {
        _formState.update {
            it.copy(savedTick = 0)
        }
    }

    fun consumeErrorTick() {
        _formState.update {
            it.copy(errorTick = 0)
        }
    }
}
package com.homan.dastkhosh.presentation.expense

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.homan.dastkhosh.core.util.HistoryDates
import com.homan.dastkhosh.core.util.historyPercentText
import com.homan.dastkhosh.core.util.toMoney
import com.homan.dastkhosh.core.util.toPersianDateTime
import com.homan.dastkhosh.core.util.toPersianDigits
import com.homan.dastkhosh.core.util.toShortToman
import com.homan.dastkhosh.domain.models.Expense
import com.homan.dastkhosh.peresantation.theme.BrandGradient
import kotlinx.coroutines.delay


@Composable
fun ExpenseHistoryScreen(
    viewModel: ExpenseViewModel,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
    bottomSpace: Dp = 110.dp
) {
    val state by viewModel.historyState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var showCustomRangeDialog by rememberSaveable {
        mutableStateOf(false)
    }

    CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Rtl
    ) {
        if (showCustomRangeDialog) {
            CustomHistoryRangeDialog(
                initialRange = state.customRange,
                onDismiss = {
                    showCustomRangeDialog = false
                },
                onConfirm = { start, endInclusive ->
                    viewModel.setCustomRange(
                        start = start,
                        endInclusive = endInclusive
                    )
                    showCustomRangeDialog = false
                }
            )
        }

        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .imePadding()
        ) {
            HistoryHeader(
                count = state.filteredExpenses.size,
                total = state.totalAmount,
                onSettingsClick = onNavigateToSettings
            )

            ExpenseSearchField(
                query = searchQuery,
                onQueryChange = viewModel::onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 20.dp,
                        vertical = 12.dp
                    )
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    top = 4.dp,
                    bottom = bottomSpace
                ),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item(key = "filters") {
                    FilterRow(
                        currentFilter = state.filter,
                        onFilterSelected = { filter ->
                            if (filter == TimeFilter.CUSTOM) {
                                showCustomRangeDialog = true
                            } else {
                                viewModel.setTimeFilter(filter)
                            }
                        }
                    )
                }

                if (state.filter == TimeFilter.CUSTOM) {
                    val range = state.customRange

                    if (range != null) {
                        item(key = "custom_range") {
                            OutlinedButton(
                                onClick = {
                                    showCustomRangeDialog = true
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = (
                                                "${HistoryDates.format(range.start)} تا " +
                                                        HistoryDates.format(range.endInclusive)
                                                ),
                                        textAlign = TextAlign.Center
                                    )

                                    Text(
                                        text = "تغییر بازه",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }
                }

                item(key = "financial_summary") {
                    FinancialSummaryCard(
                        summary = state.summary,
                        searchQuery = state.searchQuery
                    )
                }

                if (state.insights.isNotEmpty()) {
                    item(key = "financial_insights") {
                        FinancialInsightsCard(
                            insights = state.insights
                        )
                    }
                }

                if (state.chartData.isNotEmpty()) {
                    item(key = "chart") {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 18.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "توزیع هزینه‌ها",
                                    modifier = Modifier.padding(horizontal = 18.dp),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                SmartExpenseChart(
                                    timeFilter = state.filter,
                                    chartData = state.chartData,
                                    totalAmount = state.totalAmount,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                item(key = "expenses_title") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ریز هزینه‌ها",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = (
                                    "${state.filteredExpenses.size} مورد"
                                    ).toPersianDigits(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (state.filteredExpenses.isEmpty()) {
                    item(key = "empty") {
                        EmptyState(
                            isSearching = state.searchQuery.isNotBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                        )
                    }
                } else {
                    itemsIndexed(
                        items = state.filteredExpenses,
                        key = { _, expense -> expense.id }
                    ) { index, expense ->
                        AnimatedExpenseRow(
                            expense = expense,
                            index = index,
                            onDelete = {
                                viewModel.deleteExpense(expense)
                            }
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun ExpenseSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        singleLine = true,
        shape = RoundedCornerShape(18.dp),
        placeholder = {
            Text(
                text = "جستجوی هزینه‌ها...",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = null
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(
                    onClick = {
                        onQueryChange("")
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "پاک‌کردن جستجو"
                    )
                }
            }
        },
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = {
                focusManager.clearFocus()
            }
        )
    )
}


@Composable
private fun FilterRow(
    currentFilter: TimeFilter,
    onFilterSelected: (TimeFilter) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = TimeFilter.entries.toList(),
            key = { it.name }
        ) { filter ->
            FilterChip(
                selected = filter == currentFilter,
                onClick = {
                    onFilterSelected(filter)
                },
                label = {
                    Text(filter.title)
                },
                shape = RoundedCornerShape(16.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}


@Composable
private fun FinancialSummaryCard(
    summary: FinancialSummary,
    searchQuery: String
) {
    val previousAmount = summary.previousAmount
    val change = summary.changePercent

    val changeColor = when {
        previousAmount == null -> {
            MaterialTheme.colorScheme.onSurfaceVariant
        }

        summary.currentAmount > previousAmount -> {
            MaterialTheme.colorScheme.error
        }

        summary.currentAmount < previousAmount -> {
            MaterialTheme.colorScheme.primary
        }

        else -> {
            MaterialTheme.colorScheme.onSurfaceVariant
        }
    }

    val changeText = when {
        previousAmount == null -> {
            "برای مقایسه، یک بازهٔ زمانی انتخاب کن."
        }

        previousAmount == 0L && summary.currentAmount > 0L -> {
            "در دورهٔ قبل هزینه‌ای ثبت نشده؛ درصد تغییر قابل محاسبه نیست."
        }

        summary.currentAmount == previousAmount -> {
            "بدون تغییر نسبت به دورهٔ قبل"
        }

        change != null && change > 0.0 -> {
            "↑ ${historyPercentText(change)}٪ بیشتر از دورهٔ قبل"
        }

        change != null && change < 0.0 -> {
            "↓ ${historyPercentText(change)}٪ کمتر از دورهٔ قبل"
        }

        else -> {
            "مقایسه در دسترس نیست."
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
                .copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "📊 تحلیل مالی",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (searchQuery.isNotBlank()) {
                Text(
                    text = "گزارش نتایج جستجوی «$searchQuery»",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = summary.currentLabel.ifBlank { "گزارش هزینه‌ها" },
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "${summary.currentAmount.toMoney()} تومان",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = changeText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = changeColor
            )

            if (previousAmount != null) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                val maxAmount = maxOf(
                    summary.currentAmount,
                    previousAmount,
                    1L
                )

                PeriodComparisonBar(
                    title = summary.currentLabel,
                    amount = summary.currentAmount,
                    maxAmount = maxAmount,
                    barColor = MaterialTheme.colorScheme.primary
                )

                PeriodComparisonBar(
                    title = summary.previousLabel ?: "دورهٔ قبل",
                    amount = previousAmount,
                    maxAmount = maxAmount,
                    barColor = MaterialTheme.colorScheme.secondary
                )

                summary.comparisonNote?.let { note ->
                    Text(
                        text = note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            summary.topCategory?.let { category ->
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                Text(
                    text = "بیشترین هزینهٔ تجمیعی",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = (
                            "$category — " +
                                    "${summary.topCategoryAmount.toMoney()} تومان"
                            ),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = "دسته‌بندی‌ها تخمینی و بر اساس توضیحات هزینه هستند.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


@Composable
private fun PeriodComparisonBar(
    title: String,
    amount: Long,
    maxAmount: Long,
    barColor: Color
) {
    var appeared by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        appeared = true
    }

    val fraction = (
            amount.toDouble() / maxAmount.coerceAtLeast(1L).toDouble()
            ).toFloat().coerceIn(0f, 1f)

    val animatedFraction by animateFloatAsState(
        targetValue = if (appeared) fraction else 0f,
        animationSpec = tween(
            durationMillis = 650,
            easing = FastOutSlowInEasing
        ),
        label = "periodComparison"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = "${amount.toMoney()} تومان",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(50))
                .background(barColor.copy(alpha = 0.12f))
        ) {
            if (animatedFraction > 0f) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .fillMaxWidth(animatedFraction)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50))
                        .background(barColor)
                )
            }
        }
    }
}


@Composable
private fun FinancialInsightsCard(
    insights: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
                .copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "💡 نگاه دستخوش",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            insights.forEachIndexed { index, insight ->
                if (index > 0) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant
                            .copy(alpha = 0.6f)
                    )
                }

                Text(
                    text = insight,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = "این تحلیل فقط بر اساس هزینه‌های ثبت‌شده است.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


@Composable
private fun CustomHistoryRangeDialog(
    initialRange: CustomDateRange?,
    onDismiss: () -> Unit,
    onConfirm: (start: Long, endInclusive: Long) -> Unit
) {
    val today = remember {
        HistoryDates.startOfDay(System.currentTimeMillis())
    }

    var startText by rememberSaveable {
        mutableStateOf(
            HistoryDates.format(initialRange?.start ?: today)
        )
    }

    var endText by rememberSaveable {
        mutableStateOf(
            HistoryDates.format(initialRange?.endInclusive ?: today)
        )
    }

    var error by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    val focusManager = LocalFocusManager.current

    fun confirmRange() {
        val start = HistoryDates.parse(startText)
        val end = HistoryDates.parse(endText)

        val currentToday = HistoryDates.startOfDay(
            System.currentTimeMillis()
        )

        when {
            start == null || end == null -> {
                error = "تاریخ معتبر با قالب سال/ماه/روز وارد کن."
            }

            start > end -> {
                error = "تاریخ شروع نباید بعد از تاریخ پایان باشد."
            }

            end > currentToday -> {
                error = "تاریخ پایان نباید بعد از امروز باشد."
            }

            else -> {
                focusManager.clearFocus()
                onConfirm(start, end)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = "📅 بازهٔ دلخواه",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "تاریخ شمسی را به شکل ۱۴۰۵/۰۷/۰۱ وارد کن.",
                    style = MaterialTheme.typography.bodyMedium
                )

                OutlinedTextField(
                    value = startText,
                    onValueChange = {
                        startText = it.take(16)
                        error = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text("از تاریخ")
                    },
                    placeholder = {
                        Text("۱۴۰۵/۰۷/۰۱")
                    },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        textDirection = TextDirection.Ltr,
                        textAlign = TextAlign.Center
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    isError = error != null,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    )
                )

                OutlinedTextField(
                    value = endText,
                    onValueChange = {
                        endText = it.take(16)
                        error = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text("تا تاریخ")
                    },
                    placeholder = {
                        Text("۱۴۰۵/۰۷/۱۵")
                    },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        textDirection = TextDirection.Ltr,
                        textAlign = TextAlign.Center
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    isError = error != null,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            confirmRange()
                        }
                    )
                )

                Text(
                    text = "تمام هزینه‌های روز پایان هم در گزارش لحاظ می‌شوند.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                error?.let { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    confirmRange()
                }
            ) {
                Text("نمایش گزارش")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("انصراف")
            }
        }
    )
}


@Composable
fun SmartExpenseChart(
    timeFilter: TimeFilter,
    chartData: List<ChartData>,
    totalAmount: Long,
    modifier: Modifier = Modifier
) {
    if (chartData.isEmpty()) return

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (timeFilter == TimeFilter.DAILY) {
            AnimatedDonutChart(
                chartData = chartData,
                totalAmount = totalAmount
            )

            Spacer(Modifier.height(18.dp))

            ChartLegend(chartData = chartData)
        } else {
            AnimatedBarChart(
                chartData = chartData,
                totalAmount = totalAmount
            )
        }
    }
}

@Composable
private fun AnimatedDonutChart(
    chartData: List<ChartData>,
    totalAmount: Long,
    modifier: Modifier = Modifier,
    chartSize: Dp = 200.dp,
    strokeWidth: Dp = 26.dp
) {
    val progress = remember {
        Animatable(0f)
    }

    LaunchedEffect(chartData) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 1100,
                easing = FastOutSlowInEasing
            )
        )
    }

    val trackColor = MaterialTheme.colorScheme.outlineVariant
        .copy(alpha = 0.35f)

    Box(
        modifier = modifier.size(chartSize),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = strokeWidth.toPx()
                .coerceAtMost(size.minDimension / 3f)

            val inset = strokePx / 2f
            val diameter = (size.minDimension - strokePx)
                .coerceAtLeast(0f)

            val arcSize = Size(diameter, diameter)

            val topLeft = Offset(
                x = (size.width - size.minDimension) / 2f + inset,
                y = (size.height - size.minDimension) / 2f + inset
            )

            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(
                    width = strokePx,
                    cap = StrokeCap.Butt
                )
            )

            var startAngle = -90f

            chartData.forEach { data ->
                val sweep = data.percentage
                    .coerceIn(0f, 1f) * 360f * progress.value

                if (sweep > 0f) {
                    drawArc(
                        color = data.color,
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(
                            width = strokePx,
                            cap = StrokeCap.Butt
                        )
                    )

                    startAngle += sweep
                }
            }
        }

        Column(
            modifier = Modifier.padding(horizontal = strokeWidth + 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "جمع کل",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = totalAmount.toMoney(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Text(
                text = "تومان",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChartLegend(
    chartData: List<ChartData>
) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        chartData.forEach { data ->
            Row(
                modifier = Modifier.padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(data.color)
                )

                Spacer(Modifier.width(6.dp))

                Text(
                    text = data.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun AnimatedBarChart(
    chartData: List<ChartData>,
    totalAmount: Long,
    modifier: Modifier = Modifier
) {
    val progress = remember {
        Animatable(0f)
    }

    LaunchedEffect(chartData) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 900,
                easing = FastOutSlowInEasing
            )
        )
    }

    val maxAmount = (chartData.maxOfOrNull { it.amount } ?: 0L)
        .coerceAtLeast(1L)

    val maxBarHeight = 140.dp
    val barWidth = 38.dp

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "${totalAmount.toMoney()} تومان",
            modifier = Modifier.padding(horizontal = 16.dp),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(22.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(
                space = 14.dp,
                alignment = Alignment.CenterHorizontally
            ),
            verticalAlignment = Alignment.Top
        ) {
            chartData.forEach { data ->
                val fraction = (
                        data.amount.toDouble() / maxAmount.toDouble()
                        ).toFloat().coerceIn(0f, 1f)

                val currentHeight = (
                        maxBarHeight.value * fraction * progress.value
                        ).dp

                Column(
                    modifier = Modifier.width(84.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = data.amount
                                .toShortToman()
                                .replace(" تومان", ""),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(barWidth)
                            .height(maxBarHeight)
                            .clip(
                                RoundedCornerShape(
                                    topStart = 10.dp,
                                    topEnd = 10.dp
                                )
                            )
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant
                                    .copy(alpha = 0.4f)
                            ),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(currentHeight)
                                .clip(
                                    RoundedCornerShape(
                                        topStart = 10.dp,
                                        topEnd = 10.dp
                                    )
                                )
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            data.color.copy(alpha = 0.7f),
                                            data.color
                                        )
                                    )
                                )
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = data.label,
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = "مبالغ به تومان",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


@Composable
private fun HistoryHeader(
    count: Int,
    total: Long,
    onSettingsClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(
                    bottomStart = 36.dp,
                    bottomEnd = 36.dp
                )
            )
            .background(BrandGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(
                    horizontal = 22.dp,
                    vertical = 20.dp
                ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "گزارش مالی",
                    modifier = Modifier.weight(1f),
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black
                )

                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = Color.White.copy(alpha = 0.15f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = "تنظیمات",
                        tint = Color.White
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatPill(
                    modifier = Modifier.weight(1f),
                    title = "تعداد در گزارش",
                    value = "$count مورد".toPersianDigits()
                )

                StatPill(
                    modifier = Modifier.weight(1.4f),
                    title = "مجموع گزارش",
                    value = "${total.toMoney()} تومان"
                )
            }
        }
    }
}

@Composable
private fun StatPill(
    modifier: Modifier = Modifier,
    title: String,
    value: String
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = 0.18f)
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = 14.dp,
                vertical = 10.dp
            ),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                color = Color.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.labelSmall
            )

            AnimatedContent(
                targetState = value,
                transitionSpec = {
                    (
                            slideInVertically { it } + fadeIn()
                            ) togetherWith (
                            slideOutVertically { -it } + fadeOut()
                            )
                },
                label = "statValue"
            ) { currentValue ->
                Text(
                    text = currentValue,
                    color = Color.White,
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AnimatedExpenseRow(
    expense: Expense,
    index: Int,
    onDelete: () -> Unit
) {
    var appeared by remember(expense.id) {
        mutableStateOf(false)
    }

    var showDeleteDialog by rememberSaveable(expense.id) {
        mutableStateOf(false)
    }

    LaunchedEffect(expense.id) {
        delay(index.coerceAtMost(8) * 55L)
        appeared = true
    }

    val progress by animateFloatAsState(
        targetValue = if (appeared) 1f else 0f,
        animationSpec = spring(
            dampingRatio = 0.75f,
            stiffness = Spring.StiffnessLow
        ),
        label = "rowAppear"
    )

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value != SwipeToDismissBoxValue.Settled) {
                showDeleteDialog = true
            }
            false
        }
    )

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
            },
            title = {
                Text(
                    text = "حذف هزینه",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Text(
                    text = "آیا می‌خواهید هزینهٔ «${expense.description}» را حذف کنید؟",
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    }
                ) {
                    Text(
                        text = "بله، حذف کن",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                    }
                ) {
                    Text("انصراف")
                }
            }
        )
    }

    Box(
        modifier = Modifier.graphicsLayer {
            alpha = progress
            translationY = (1f - progress) * 60f
            scaleX = 0.94f + progress * 0.06f
            scaleY = 0.94f + progress * 0.06f
        }
    ) {
        SwipeToDismissBox(
            state = dismissState,
            backgroundContent = {
                DeleteBackground()
            },
            content = {
                ExpenseCard(
                    expense = expense,
                    onDelete = {
                        showDeleteDialog = true
                    }
                )
            }
        )
    }
}

@Composable
private fun DeleteBackground() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(24.dp))
            .background(
                MaterialTheme.colorScheme.error.copy(alpha = 0.16f)
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.DeleteOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )

            Text(
                text = "برای حذف بکشید",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelLarge
            )

            Icon(
                imageVector = Icons.Rounded.DeleteOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}


@Composable
private fun ExpenseCard(
    expense: Expense,
    onDelete: () -> Unit
) {
    var expanded by rememberSaveable(expense.id) {
        mutableStateOf(false)
    }

    Card(
        onClick = {
            expanded = !expanded
        },
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = 0.8f,
                    stiffness = Spring.StiffnessMediumLow
                )
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .background(
                            MaterialTheme.colorScheme.primaryContainer
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ReceiptLong,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = expense.description,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = if (expanded) Int.MAX_VALUE else 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(13.dp)
                        )

                        Spacer(Modifier.width(4.dp))

                        Text(
                            text = expense.createdAt
                                .toPersianDateTime()
                                .toPersianDigits(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = expense.amount.toMoney(),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(Modifier.width(5.dp))

                Text(
                    text = "تومان",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(
                    animationSpec = spring(
                        stiffness = Spring.StiffnessMediumLow
                    )
                ) + fadeIn(),
                exit = shrinkVertically(
                    animationSpec = tween(200)
                ) + fadeOut(tween(120))
            ) {
                Column {
                    Spacer(Modifier.height(12.dp))

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline
                            .copy(alpha = 0.4f)
                    )

                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        FilledTonalButton(
                            onClick = onDelete,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.error
                                    .copy(alpha = 0.14f),
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DeleteOutline,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )

                            Spacer(Modifier.width(6.dp))

                            Text("حذف هزینه")
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun EmptyState(
    modifier: Modifier = Modifier,
    isSearching: Boolean = false
) {
    val floatingTransition = rememberInfiniteTransition(
        label = "emptyFloat"
    )

    val offsetY by floatingTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1800,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatY"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .graphicsLayer {
                    translationY = offsetY
                }
                .clip(RoundedCornerShape(32.dp))
                .background(
                    MaterialTheme.colorScheme.primaryContainer
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isSearching) {
                    Icons.Rounded.Search
                } else {
                    Icons.Rounded.ReceiptLong
                },
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(Modifier.height(18.dp))

        Text(
            text = if (isSearching) {
                "هزینه‌ای پیدا نشد"
            } else {
                "در این بازه هزینه‌ای ثبت نشده"
            },
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = if (isSearching) {
                "عبارت جستجو یا بازهٔ زمانی را تغییر بده."
            } else {
                "برای دیدن هزینه‌های دیگر، بازهٔ زمانی را تغییر بده."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
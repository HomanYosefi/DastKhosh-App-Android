package com.homan.dastkhosh.presentation.expense

import android.icu.util.Calendar as IcuCalendar
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.homan.dastkhosh.core.util.HistoryDates
import com.homan.dastkhosh.core.util.toMoney
import com.homan.dastkhosh.core.util.toPersianDigits
import com.homan.dastkhosh.core.util.toShortToman
import com.homan.dastkhosh.peresantation.theme.BrandGradient
import kotlinx.coroutines.delay

private val quickAmounts = listOf(
    10_000L,
    50_000L,
    100_000L,
    500_000L,
    1_000_000L
)

private val defaultDescriptions = listOf(
    "خرید روزمره",
    "بنزین",
    "ناهار",
    "اسنپ",
    "قهوه"
)

enum class FormStep {
    AMOUNT,
    DESCRIPTION
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddExpenseScreen(
    viewModel: ExpenseViewModel,
    modifier: Modifier = Modifier,
    bottomSpace: Dp = 110.dp
) {
    val state by viewModel.formState.collectAsState()
    val expenses by viewModel.expenses.collectAsState()
    val dynamicDescriptions by viewModel.topDescriptions.collectAsState()

    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    var currentStep by rememberSaveable {
        mutableStateOf(FormStep.AMOUNT)
    }

    var localAmountError by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    var localErrorTick by remember {
        mutableIntStateOf(0)
    }

    var showSuccess by remember {
        mutableStateOf(false)
    }

    val now by produceState(
        initialValue = System.currentTimeMillis()
    ) {
        while (true) {
            value = System.currentTimeMillis()
            delay(60_000L)
        }
    }

    val todayStart = remember(now) {
        HistoryDates.startOfDay(now)
    }

    val monthStart = remember(todayStart) {
        HistoryDates.calendar(todayStart).apply {
            set(IcuCalendar.DAY_OF_MONTH, 1)
        }.timeInMillis
    }


    val totalAmount = remember(expenses) {
        expenses.sumOf { it.amount }
    }

    val todayTotal = remember(expenses, todayStart, now) {
        expenses
            .filter { it.createdAt >= todayStart && it.createdAt <= now }
            .sumOf { it.amount }
    }

    val monthTotal = remember(expenses, monthStart, now) {
        expenses
            .filter { it.createdAt >= monthStart && it.createdAt <= now }
            .sumOf { it.amount }
    }

    val suggestions = remember(dynamicDescriptions) {
        dynamicDescriptions
            .ifEmpty { defaultDescriptions }
            .distinct()
            .take(5)
    }

    val busy = state.isSaving || showSuccess || state.savedTick > 0

    val amountError = localAmountError ?: state.amountError
    val shake = remember { Animatable(0f) }

    fun goToNextStep() {
        if (busy) return

        if (state.amountValue <= 0L) {
            localAmountError = "مبلغی بیشتر از صفر وارد کنید"
            localErrorTick++
            return
        }

        localAmountError = null
        focusManager.clearFocus()
        currentStep = FormStep.DESCRIPTION
    }

    fun goToAmountStep() {
        if (busy) return

        focusManager.clearFocus()
        currentStep = FormStep.AMOUNT
    }

    fun saveExpense() {
        if (busy) return

        focusManager.clearFocus()
        viewModel.save()
    }

    BackHandler(
        enabled = currentStep == FormStep.DESCRIPTION || busy
    ) {
        if (!busy) {
            goToAmountStep()
        }
    }

    LaunchedEffect(currentStep) {
        scrollState.animateScrollTo(0)
    }

    LaunchedEffect(state.errorTick, localErrorTick) {
        if (state.errorTick == 0 && localErrorTick == 0) {
            return@LaunchedEffect
        }

        if (state.amountError != null) {
            currentStep = FormStep.AMOUNT
        }

        shake.snapTo(0f)

        shake.animateTo(
            targetValue = 0f,
            animationSpec = keyframes {
                durationMillis = 420
                0f at 0
                18f at 60
                -18f at 120
                12f at 190
                -12f at 260
                5f at 340
                0f at 420
            }
        )

        if (state.errorTick > 0) {
            viewModel.consumeErrorTick()
        }

        localErrorTick = 0
    }

    LaunchedEffect(state.savedTick) {
        if (state.savedTick <= 0) return@LaunchedEffect

        focusManager.clearFocus()
        localAmountError = null
        currentStep = FormStep.AMOUNT

        showSuccess = true

        try {
            delay(1_600L)
        } finally {
            showSuccess = false
            viewModel.consumeSavedTick()
        }
    }

    CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Rtl
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .imePadding()
        ) {
            AddExpenseHeader(total = totalAmount)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Spacer(Modifier.height(2.dp))

                RegistrationSteps(currentStep = currentStep)

                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        if (targetState == FormStep.DESCRIPTION) {
                            (
                                    slideInHorizontally { -it } + fadeIn()
                                    ) togetherWith (
                                    slideOutHorizontally { it } + fadeOut()
                                    )
                        } else {
                            (
                                    slideInHorizontally { it } + fadeIn()
                                    ) togetherWith (
                                    slideOutHorizontally { -it } + fadeOut()
                                    )
                        }
                    },
                    label = "expenseFormStep"
                ) { step ->
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        when (step) {
                            FormStep.AMOUNT -> {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .graphicsLayer {
                                            translationX = shake.value
                                        },
                                    shape = RoundedCornerShape(26.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor =
                                            MaterialTheme.colorScheme.surface
                                    ),
                                    elevation = CardDefaults.cardElevation(
                                        defaultElevation = 2.dp
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(20.dp),
                                        verticalArrangement =
                                            Arrangement.spacedBy(14.dp)
                                    ) {
                                        AddSectionTitle(
                                            icon = Icons.Rounded.ShoppingCart,
                                            text = "مبلغ هزینه"
                                        )

                                        Text(
                                            text = state.amountValue.toMoney(),
                                            modifier = Modifier.fillMaxWidth(),
                                            fontSize = 30.sp,
                                            fontWeight = FontWeight.Black,
                                            color = if (state.amountValue > 0L) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme
                                                    .onSurfaceVariant
                                                    .copy(alpha = 0.5f)
                                            },
                                            textAlign = TextAlign.Center
                                        )

                                        Text(
                                            text = "تومان",
                                            modifier = Modifier.fillMaxWidth(),
                                            style =
                                                MaterialTheme.typography.labelLarge,
                                            textAlign = TextAlign.Center,
                                            color = MaterialTheme.colorScheme
                                                .onSurfaceVariant
                                        )

                                        OutlinedTextField(
                                            value = state.amount.toPersianDigits(),
                                            onValueChange = {
                                                localAmountError = null
                                                viewModel.onAmountChange(it)
                                            },
                                            enabled = !busy,
                                            modifier = Modifier.fillMaxWidth(),
                                            label = {
                                                Text("مبلغ به تومان")
                                            },
                                            placeholder = {
                                                Text("مثال: ۲۵۰۰۰۰")
                                            },
                                            textStyle =
                                                MaterialTheme.typography.titleMedium
                                                    .copy(
                                                        textAlign = TextAlign.Center,
                                                        textDirection =
                                                            TextDirection.Ltr
                                                    ),
                                            trailingIcon = {
                                                if (state.amount.isNotEmpty()) {
                                                    IconButton(
                                                        enabled = !busy,
                                                        onClick = {
                                                            localAmountError = null
                                                            viewModel.clearAmount()
                                                        }
                                                    ) {
                                                        Icon(
                                                            imageVector =
                                                                Icons.Rounded.Close,
                                                            contentDescription =
                                                                "پاک‌کردن مبلغ"
                                                        )
                                                    }
                                                }
                                            },
                                            isError = amountError != null,
                                            supportingText = {
                                                Text(
                                                    text = amountError
                                                        ?: if (state.amountValue > 0L) {
                                                            state.amountValue
                                                                .toShortToman()
                                                        } else {
                                                            "فقط عدد وارد کنید"
                                                        },
                                                    color = if (amountError != null) {
                                                        MaterialTheme.colorScheme.error
                                                    } else {
                                                        MaterialTheme.colorScheme
                                                            .onSurfaceVariant
                                                    }
                                                )
                                            },
                                            keyboardOptions = KeyboardOptions(
                                                keyboardType = KeyboardType.Number,
                                                imeAction = ImeAction.Next
                                            ),
                                            keyboardActions = KeyboardActions(
                                                onNext = {
                                                    goToNextStep()
                                                }
                                            ),
                                            singleLine = true,
                                            shape = RoundedCornerShape(18.dp)
                                        )

                                        Text(
                                            text = "افزودن سریع به مبلغ",
                                            style =
                                                MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme
                                                .onSurfaceVariant
                                        )

                                        FlowRow(
                                            horizontalArrangement =
                                                Arrangement.spacedBy(8.dp),
                                            verticalArrangement =
                                                Arrangement.spacedBy(4.dp)
                                        ) {
                                            quickAmounts.forEach { value ->
                                                AddExpenseChip(
                                                    label = "+ ${
                                                        value.toShortToman()
                                                            .replace(" تومان", "")
                                                    }",
                                                    enabled = !busy,
                                                    onClick = {
                                                        localAmountError = null
                                                        viewModel.addQuickAmount(value)
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                Button(
                                    onClick = { goToNextStep() },
                                    enabled = !busy,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 58.dp),
                                    shape = RoundedCornerShape(22.dp)
                                ) {
                                    Text(
                                        text = "مرحله بعدی",
                                        style = MaterialTheme.typography.titleMedium
                                    )

                                    Spacer(Modifier.width(8.dp))

                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded
                                            .ArrowForward,
                                        contentDescription = null
                                    )
                                }
                            }

                            FormStep.DESCRIPTION -> {
                                AmountReviewCard(
                                    amount = state.amountValue,
                                    enabled = !busy,
                                    onEdit = { goToAmountStep() }
                                )

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .graphicsLayer {
                                            translationX = shake.value
                                        },
                                    shape = RoundedCornerShape(26.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor =
                                            MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(20.dp),
                                        verticalArrangement =
                                            Arrangement.spacedBy(14.dp)
                                    ) {
                                        AddSectionTitle(
                                            icon = Icons.Rounded.Edit,
                                            text = "توضیحات هزینه"
                                        )

                                        OutlinedTextField(
                                            value = state.description,
                                            onValueChange =
                                                viewModel::onDescriptionChange,
                                            enabled = !busy,
                                            modifier = Modifier.fillMaxWidth(),
                                            label = {
                                                Text("این هزینه برای چه بود؟")
                                            },
                                            placeholder = {
                                                Text("مثال: خرید بنزین")
                                            },
                                            minLines = 2,
                                            maxLines = 4,
                                            isError =
                                                state.descriptionError != null,
                                            supportingText = {
                                                Column(
                                                    verticalArrangement =
                                                        Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Text(
                                                        text = state.descriptionError
                                                            ?: "کوتاه و مشخص بنویسید",
                                                        color = if (
                                                            state.descriptionError != null
                                                        ) {
                                                            MaterialTheme.colorScheme.error
                                                        } else {
                                                            MaterialTheme.colorScheme
                                                                .onSurfaceVariant
                                                        }
                                                    )

                                                    Text(
                                                        text = (
                                                                "${state.description.length}/120"
                                                                ).toPersianDigits(),
                                                        modifier =
                                                            Modifier.fillMaxWidth(),
                                                        textAlign = TextAlign.End
                                                    )
                                                }
                                            },
                                            keyboardOptions = KeyboardOptions(
                                                imeAction = ImeAction.Done
                                            ),
                                            keyboardActions = KeyboardActions(
                                                onDone = {
                                                    saveExpense()
                                                }
                                            ),
                                            shape = RoundedCornerShape(18.dp)
                                        )

                                        Text(
                                            text = if (
                                                dynamicDescriptions.isNotEmpty()
                                            ) {
                                                "پیشنهادهای پرتکرار شما"
                                            } else {
                                                "پیشنهاد برای شروع"
                                            },
                                            style =
                                                MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme
                                                .onSurfaceVariant
                                        )

                                        FlowRow(
                                            horizontalArrangement =
                                                Arrangement.spacedBy(8.dp),
                                            verticalArrangement =
                                                Arrangement.spacedBy(4.dp)
                                        ) {
                                            suggestions.forEach { description ->
                                                AddExpenseChip(
                                                    label = description,
                                                    selected =
                                                        state.description.trim() ==
                                                                description,
                                                    enabled = !busy,
                                                    onClick = {
                                                        viewModel.onDescriptionChange(
                                                            description
                                                        )
                                                    }
                                                )
                                            }
                                        }

                                        Text(
                                            text = "💡 توضیح دقیق‌تر به دسته‌بندی " +
                                                    "بهتر هزینه‌ها در گزارش مالی کمک می‌کند.",
                                            style =
                                                MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme
                                                .onSurfaceVariant
                                        )
                                    }
                                }

                                AnimatedVisibility(
                                    visible = state.description.isNotBlank()
                                ) {
                                    ExpensePreviewCard(
                                        amount = state.amountValue,
                                        description = state.description.trim()
                                    )
                                }

                                AddExpenseSaveButton(
                                    enabled = !busy,
                                    isSaving = state.isSaving,
                                    onClick = { saveExpense() }
                                )

                                TextButton(
                                    onClick = { goToAmountStep() },
                                    enabled = !busy,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded
                                            .ArrowBack,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )

                                    Spacer(Modifier.width(8.dp))

                                    Text("بازگشت و ویرایش مبلغ")
                                }
                            }
                        }
                    }
                }
                QuickExpenseOverview(
                    todayAmount = todayTotal,
                    monthAmount = monthTotal,
                    monthTitle = HistoryDates.monthTitle(monthStart)
                )

                Spacer(Modifier.height(bottomSpace))
            }
        }

        if (showSuccess) {
            ExpenseSavedDialog()
        }
    }
}


@Composable
private fun AddExpenseHeader(total: Long) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(
                    bottomStart = 32.dp,
                    bottomEnd = 32.dp
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
                    vertical = 18.dp
                ),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.AccountBalanceWallet,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    text = "ثبت هزینه جدید",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Text(
                text = "جمع کل ثبت‌شده: ${total.toMoney()} تومان",
                color = Color.White.copy(alpha = 0.9f),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}


@Composable
private fun RegistrationSteps(currentStep: FormStep) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        RegistrationStep(
            number = "۱",
            title = "مبلغ",
            selected = currentStep == FormStep.AMOUNT,
            completed = currentStep == FormStep.DESCRIPTION
        )

        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant
        )

        RegistrationStep(
            number = "۲",
            title = "توضیحات و ثبت",
            selected = currentStep == FormStep.DESCRIPTION,
            completed = false
        )
    }
}

@Composable
private fun RegistrationStep(
    number: String,
    title: String,
    selected: Boolean,
    completed: Boolean
) {
    val active = selected || completed

    val background by animateColorAsState(
        targetValue = if (active) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        label = "stepBackground"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .background(background, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (completed) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = "مرحله تکمیل شده",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(
                    text = number,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (selected) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }

        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (active) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}


@Composable
private fun AmountReviewCard(
    amount: Long,
    enabled: Boolean,
    onEdit: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = "مبلغ انتخاب‌شده",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "${amount.toMoney()} تومان",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            TextButton(
                enabled = enabled,
                onClick = onEdit
            ) {
                Text("ویرایش")
            }
        }
    }
}


@Composable
private fun ExpensePreviewCard(
    amount: Long,
    description: String
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "پیش‌نمایش هزینه",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                text = "${amount.toMoney()} تومان",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "با تاریخ و ساعت زمان ثبت ذخیره می‌شود.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


@Composable
private fun QuickExpenseOverview(
    todayAmount: Long,
    monthAmount: Long,
    monthTitle: String
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "نگاه سریع به هزینه‌ها",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            OverviewAmount(
                title = "امروز",
                amount = todayAmount
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant
            )

            OverviewAmount(
                title = monthTitle,
                amount = monthAmount
            )

            Text(
                text = "فقط هزینه‌های ثبت‌شده؛ پیش‌نویس فعلی محاسبه نشده است.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun OverviewAmount(
    title: String,
    amount: Long
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = "${amount.toMoney()} تومان",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}


@Composable
private fun AddSectionTitle(
    icon: ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(Modifier.width(10.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}


@Composable
private fun AddExpenseChip(
    label: String,
    selected: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    var pressed by remember {
        mutableStateOf(false)
    }

    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "suggestionScale"
    )

    LaunchedEffect(pressed) {
        if (pressed) {
            delay(140L)
            pressed = false
        }
    }

    FilterChip(
        selected = selected,
        enabled = enabled,
        onClick = {
            pressed = true
            onClick()
        },
        modifier = Modifier.scale(scale),
        label = {
            Text(label)
        },
        shape = RoundedCornerShape(16.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}


@Composable
private fun AddExpenseSaveButton(
    enabled: Boolean,
    isSaving: Boolean,
    onClick: () -> Unit
) {
    val disabledColor = MaterialTheme.colorScheme.surfaceVariant

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                if (enabled || isSaving) {
                    BrandGradient
                } else {
                    Brush.linearGradient(
                        listOf(disabledColor, disabledColor)
                    )
                }
            )
    ) {
        Button(
            onClick = onClick,
            enabled = enabled && !isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 58.dp),
            shape = RoundedCornerShape(22.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                contentColor = Color.White,
                disabledContentColor = if (isSaving) {
                    Color.White
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            ),
            elevation = null
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )

                Spacer(Modifier.width(10.dp))

                Text("در حال ثبت...")
            } else {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    text = "ذخیره هزینه",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}


@Composable
private fun ExpenseSavedDialog() {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        val appear = remember {
            Animatable(0.85f)
        }

        LaunchedEffect(Unit) {
            appear.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = 0.6f,
                    stiffness = Spring.StiffnessLow
                )
            )
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .scale(appear.value),
            shape = RoundedCornerShape(30.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 18.dp
        ) {
            Column(
                modifier = Modifier.padding(
                    horizontal = 24.dp,
                    vertical = 28.dp
                ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .background(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(38.dp)
                    )
                }

                Text(
                    text = "ثبت شد!",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "هزینه در تاریخچه ذخیره شد.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
package com.homan.dastkhosh.core.util

import java.util.Calendar
import java.util.Locale


fun Long.toPersianDateTime(): String {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = this@toPersianDateTime
    }

    val gregorianYear = calendar.get(Calendar.YEAR)
    val gregorianMonth = calendar.get(Calendar.MONTH) + 1
    val gregorianDay = calendar.get(Calendar.DAY_OF_MONTH)

    val (persianYear, persianMonth, persianDay) = gregorianToPersian(
        year = gregorianYear,
        month = gregorianMonth,
        day = gregorianDay
    )

    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)

    return String.format(
        Locale("fa", "IR"),
        "%04d/%02d/%02d - %02d:%02d",
        persianYear,
        persianMonth,
        persianDay,
        hour,
        minute
    )
}


private fun gregorianToPersian(
    year: Int,
    month: Int,
    day: Int
): Triple<Int, Int, Int> {

    val gregorianDaysInMonth = intArrayOf(
        31, 28, 31, 30, 31, 30,
        31, 31, 30, 31, 30, 31
    )

    val persianDaysInMonth = intArrayOf(
        31, 31, 31, 31, 31, 31,
        30, 30, 30, 30, 30, 29
    )

    var gy = year - 1600
    val gm = month - 1
    val gd = day - 1

    var daysPassed = 365 * gy +
            (gy + 3) / 4 -
            (gy + 99) / 100 +
            (gy + 399) / 400

    for (index in 0 until gm) {
        daysPassed += gregorianDaysInMonth[index]
    }

    val isGregorianLeapYear =
        (gy % 4 == 0 && gy % 100 != 0) || gy % 400 == 0

    if (gm > 1 && isGregorianLeapYear) {
        daysPassed++
    }

    daysPassed += gd

    var jalaliDaysPassed = daysPassed - 79

    val cycleCount = jalaliDaysPassed / 12053
    jalaliDaysPassed %= 12053

    var jy = 979 + (33 * cycleCount) + (4 * (jalaliDaysPassed / 1461))
    jalaliDaysPassed %= 1461

    if (jalaliDaysPassed >= 366) {
        jy += (jalaliDaysPassed - 1) / 365
        jalaliDaysPassed = (jalaliDaysPassed - 1) % 365
    }

    var jm = 0

    while (jm < 11 && jalaliDaysPassed >= persianDaysInMonth[jm]) {
        jalaliDaysPassed -= persianDaysInMonth[jm]
        jm++
    }

    val jd = jalaliDaysPassed + 1

    return Triple(
        jy,
        jm + 1,
        jd
    )
}


private val PERSIAN_DIGITS = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

fun String.toPersianDigits(): String = buildString {
    this@toPersianDigits.forEach { c ->
        append(if (c in '0'..'9') PERSIAN_DIGITS[c - '0'] else c)
    }
}

fun String.digitsOnly(): String = buildString {
    this@digitsOnly.forEach { c ->
        when (c) {
            in '0'..'9' -> append(c)
            in '۰'..'۹' -> append(c - '۰')
            in '٠'..'٩' -> append(c - '٠')
        }
    }
}

fun Long.toMoney(): String = toString()
    .reversed()
    .chunked(3)
    .joinToString("٬")
    .reversed()
    .toPersianDigits()

fun String.toMoneyOrEmpty(): String =
    digitsOnly().trimStart('0').toLongOrNull()?.toMoney() ?: ""

fun Long.toShortToman(): String = when {
    this >= 1_000_000_000 -> "${(this / 100_000_000).toString().insertDot()} میلیارد تومان".toPersianDigits()
    this >= 1_000_000 -> "${(this / 100_000).toString().insertDot()} میلیون تومان".toPersianDigits()
    this >= 1_000 -> "${this / 1_000} هزار تومان".toPersianDigits()
    else -> "$this تومان".toPersianDigits()
}

private fun String.insertDot(): String =
    if (length <= 1) this
    else {
        val head = dropLast(1)
        val tail = last()
        if (tail == '0') head else "$head.$tail"
    }
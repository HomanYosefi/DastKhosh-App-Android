package com.homan.dastkhosh.core.util

import android.icu.util.Calendar as IcuCalendar
import android.icu.util.ULocale
import java.util.Date
import java.util.Locale
import kotlin.math.abs

internal object HistoryDates {

    private val monthNames = listOf(
        "فروردین",
        "اردیبهشت",
        "خرداد",
        "تیر",
        "مرداد",
        "شهریور",
        "مهر",
        "آبان",
        "آذر",
        "دی",
        "بهمن",
        "اسفند"
    )

    fun calendar(timestamp: Long): IcuCalendar {
        return IcuCalendar.getInstance(
            ULocale("fa_IR@calendar=persian")
        ).apply {
            timeInMillis = timestamp
        }
    }

    fun startOfDay(timestamp: Long): Long {
        return calendar(timestamp).apply {
            set(IcuCalendar.HOUR_OF_DAY, 0)
            set(IcuCalendar.MINUTE, 0)
            set(IcuCalendar.SECOND, 0)
            set(IcuCalendar.MILLISECOND, 0)
        }.timeInMillis
    }

    fun shift(
        timestamp: Long,
        field: Int,
        amount: Int
    ): Long {
        return calendar(timestamp).apply {
            add(field, amount)
        }.timeInMillis
    }

    fun addDays(timestamp: Long, days: Int): Long {
        return shift(
            timestamp = timestamp,
            field = IcuCalendar.DAY_OF_MONTH,
            amount = days
        )
    }

    fun monthTitle(timestamp: Long): String {
        val cal = calendar(timestamp)
        val monthName = monthNames[cal[IcuCalendar.MONTH]]
        val year = cal[IcuCalendar.YEAR]

        return "$monthName $year".toPersianDigits()
    }

    fun yearTitle(timestamp: Long): String {
        val year = calendar(timestamp)[IcuCalendar.YEAR]

        return "سال $year".toPersianDigits()
    }

    fun format(timestamp: Long): String {
        val cal = calendar(timestamp)

        return String.format(
            Locale.US,
            "%04d/%02d/%02d",
            cal[IcuCalendar.YEAR],
            cal[IcuCalendar.MONTH] + 1,
            cal[IcuCalendar.DAY_OF_MONTH]
        ).toPersianDigits()
    }

    fun englishDigits(value: String): String {
        return buildString {
            value.forEach { char ->
                append(
                    when (char) {
                        in '۰'..'۹' -> '0' + (char - '۰')
                        in '٠'..'٩' -> '0' + (char - '٠')
                        else -> char
                    }
                )
            }
        }
    }



    fun parse(value: String): Long? {
        val normalized = englishDigits(value)
            .trim()
            .replace('-', '/')
            .replace("\\s+".toRegex(), "")

        val match = Regex("""^(\d{4})/(\d{1,2})/(\d{1,2})$""")
            .matchEntire(normalized)
            ?: return null

        val year = match.groupValues[1].toIntOrNull() ?: return null
        val month = match.groupValues[2].toIntOrNull() ?: return null
        val day = match.groupValues[3].toIntOrNull() ?: return null

        if (
            year !in 1200..1600 ||
            month !in 1..12 ||
            day !in 1..31
        ) {
            return null
        }

        return runCatching {
            calendar(System.currentTimeMillis()).apply {
                clear()
                isLenient = false

                set(IcuCalendar.YEAR, year)
                set(IcuCalendar.MONTH, month - 1)
                set(IcuCalendar.DAY_OF_MONTH, day)
                set(IcuCalendar.HOUR_OF_DAY, 0)
                set(IcuCalendar.MINUTE, 0)
                set(IcuCalendar.SECOND, 0)
                set(IcuCalendar.MILLISECOND, 0)
            }.timeInMillis
        }.getOrNull()
    }


    fun inclusiveDayCount(
        start: Long,
        endInclusive: Long
    ): Int {
        val normalizedStart = startOfDay(start)
        val normalizedEnd = startOfDay(endInclusive)

        require(normalizedStart <= normalizedEnd) {
            "Start date must not be after end date."
        }

        return calendar(normalizedStart).fieldDifference(
            Date(normalizedEnd),
            IcuCalendar.DAY_OF_MONTH
        ) + 1
    }
}

internal fun historyPercentText(value: Double): String {
    return String.format(
        Locale.US,
        "%.1f",
        abs(value)
    )
        .trimEnd('0')
        .trimEnd('.')
        .toPersianDigits()
}
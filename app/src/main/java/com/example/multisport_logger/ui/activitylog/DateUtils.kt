package com.example.multisport_logger.ui.activitylog

import com.example.multisport_logger.data.util.CurrencyUtil
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

object DateFormatterUtil {
    private val isoFormatter = DateTimeFormatter.ISO_LOCAL_DATE // YYYY-MM-DD
    private val csLocale = Locale.forLanguageTag("cs-CZ")
    private val displayFormatter = DateTimeFormatter.ofPattern("EEE, d. M. yyyy", csLocale)
    private val monthYearFormatter = DateTimeFormatter.ofPattern("LLLL yyyy", csLocale)

    fun todayIso(): String {
        return LocalDate.now().format(isoFormatter)
    }

    fun currentMonthKey(): String {
        return YearMonth.now().toString() // "YYYY-MM"
    }

    fun isValidYearMonth(yearMonthStr: String): Boolean {
        return try {
            YearMonth.parse(yearMonthStr)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun previousMonthKey(monthKey: String): String {
        return try {
            YearMonth.parse(monthKey).minusMonths(1).toString()
        } catch (e: Exception) {
            monthKey
        }
    }

    fun nextMonthKey(monthKey: String): String {
        return try {
            YearMonth.parse(monthKey).plusMonths(1).toString()
        } catch (e: Exception) {
            monthKey
        }
    }

    fun formatToDisplay(isoDate: String): String {
        return try {
            val dateStr = if (isoDate.length >= 10) isoDate.take(10) else isoDate
            val date = LocalDate.parse(dateStr, isoFormatter)
            date.format(displayFormatter)
        } catch (e: Exception) {
            isoDate
        }
    }

    fun formatToMonthYear(isoDateOrMonthKey: String): String {
        return try {
            val formatted = if (isoDateOrMonthKey.length == 7) { // "2025-05"
                val ym = YearMonth.parse(isoDateOrMonthKey)
                ym.format(monthYearFormatter)
            } else {
                val dateStr = if (isoDateOrMonthKey.length >= 10) isoDateOrMonthKey.take(10) else isoDateOrMonthKey
                val date = LocalDate.parse(dateStr, isoFormatter)
                date.format(monthYearFormatter)
            }
            formatted.replaceFirstChar { if (it.isLowerCase()) it.titlecase(csLocale) else it.toString() }
        } catch (e: Exception) {
            isoDateOrMonthKey
        }
    }

    fun extractMonthKey(isoDate: String): String {
        return if (isoDate.length >= 7) isoDate.substring(0, 7) else isoDate
    }

    fun isValidIsoDate(dateStr: String): Boolean {
        return try {
            val d = if (dateStr.length >= 10) dateStr.take(10) else dateStr
            LocalDate.parse(d, isoFormatter)
            true
        } catch (e: DateTimeParseException) {
            false
        }
    }

    fun formatCurrency(amount: Double): String {
        return CurrencyUtil.formatKcz(amount)
    }

    fun formatVisits(count: Int): String {
        return when {
            count == 1 -> "1 návštěva"
            count in 2..4 -> "$count návštěvy"
            else -> "$count návštěv"
        }
    }
}

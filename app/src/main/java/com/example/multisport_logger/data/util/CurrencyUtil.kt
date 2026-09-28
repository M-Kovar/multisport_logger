package com.example.multisport_logger.data.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs
import kotlin.math.round

object CurrencyUtil {
    fun formatKcz(amount: Double): String {
        val symbols = DecimalFormatSymbols(Locale.forLanguageTag("cs-CZ")).apply {
            groupingSeparator = ' '
            decimalSeparator = ','
        }
        val isWhole = abs(amount - round(amount)) < 0.001
        val pattern = if (isWhole) "#,##0 Kč" else "#,##0.00 Kč"
        val formatter = DecimalFormat(pattern, symbols)
        return formatter.format(amount)
    }
}

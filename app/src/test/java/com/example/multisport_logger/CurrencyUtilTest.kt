package com.example.multisport_logger

import com.example.multisport_logger.data.util.CurrencyUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CurrencyUtilTest {

    @Test
    fun formatKcz_formatsWithKcSuffix() {
        val result150 = CurrencyUtil.formatKcz(150.0)
        assertEquals("150 Kč", result150)
        assertTrue(result150.endsWith(" Kč"))

        val result1040 = CurrencyUtil.formatKcz(1040.0)
        assertEquals("1 040 Kč", result1040)
        assertTrue(result1040.endsWith(" Kč"))

        val resultZero = CurrencyUtil.formatKcz(0.0)
        assertEquals("0 Kč", resultZero)

        val resultLarge = CurrencyUtil.formatKcz(1234567.0)
        assertEquals("1 234 567 Kč", resultLarge)

        val resultDecimal = CurrencyUtil.formatKcz(1040.50)
        assertEquals("1 040,50 Kč", resultDecimal)

        val resultNegative = CurrencyUtil.formatKcz(-150.0)
        assertEquals("-150 Kč", resultNegative)
    }
}

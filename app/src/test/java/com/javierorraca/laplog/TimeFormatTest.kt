package com.javierorraca.laplog

import com.javierorraca.laplog.ui.formatTime
import com.javierorraca.laplog.ui.splitTime
import org.junit.Assert.assertEquals
import org.junit.Test

class TimeFormatTest {
    @Test
    fun formatsCentisecondsBelowOneHour() {
        assertEquals("00:00.00", formatTime(0))
        assertEquals("00:01.23", formatTime(1_234))
        assertEquals("59:59.99", formatTime(3_599_999))
    }

    @Test
    fun formatsHoursWhenNeeded() {
        assertEquals("1:00:00.00", formatTime(3_600_000))
        assertEquals("2:03:04.56", formatTime(7_384_560))
    }

    @Test
    fun splitsMainTimeAndCentiseconds() {
        assertEquals("12:34" to ".56", splitTime("12:34.56"))
    }
}

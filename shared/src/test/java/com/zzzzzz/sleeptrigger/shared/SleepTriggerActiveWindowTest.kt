package com.zzzzzz.sleeptrigger.shared

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId

class SleepTriggerActiveWindowTest {
    @Test
    fun acceptsOvernightSleepHours() {
        assertTrue(activeAt(2026, 6, 18, 4, 15))
        assertTrue(activeAt(2026, 6, 18, 20, 0))
        assertTrue(activeAt(2026, 6, 18, 23, 59))
    }

    @Test
    fun rejectsDaytimeFalsePositiveHours() {
        assertFalse(activeAt(2026, 6, 18, 10, 0))
        assertFalse(activeAt(2026, 6, 18, 15, 24))
        assertFalse(activeAt(2026, 6, 18, 17, 4))
        assertFalse(activeAt(2026, 6, 18, 18, 41))
    }

    private fun activeAt(year: Int, month: Int, day: Int, hour: Int, minute: Int): Boolean {
        val zone = ZoneId.of("America/Toronto")
        val millis = LocalDateTime.of(year, month, day, hour, minute)
            .atZone(zone)
            .toInstant()
            .toEpochMilli()
        return SleepTriggerActiveWindow.isActiveAt(millis, zone)
    }
}

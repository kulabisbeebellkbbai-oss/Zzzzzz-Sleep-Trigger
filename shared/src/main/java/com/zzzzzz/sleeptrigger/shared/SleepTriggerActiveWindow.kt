package com.zzzzzz.sleeptrigger.shared

import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId

object SleepTriggerActiveWindow {
    private val start = LocalTime.of(20, 0)
    private val end = LocalTime.of(10, 0)

    fun isActiveAt(epochMillis: Long, zoneId: ZoneId = ZoneId.systemDefault()): Boolean {
        val localTime = Instant.ofEpochMilli(epochMillis).atZone(zoneId).toLocalTime()
        return !localTime.isBefore(start) || localTime.isBefore(end)
    }

    const val DESCRIPTION = "20:00-10:00"
}

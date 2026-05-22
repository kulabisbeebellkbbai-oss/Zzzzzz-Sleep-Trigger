package com.zzzzzz.sleeptrigger.health

data class CompletedSleepSession(
    val id: String,
    val startMillis: Long,
    val endMillis: Long,
    val sourcePackage: String
)

interface HealthConnectSleepImporter {
    fun importCompletedSleepSessions(sinceMillis: Long): List<CompletedSleepSession>
}

class NoopHealthConnectSleepImporter : HealthConnectSleepImporter {
    override fun importCompletedSleepSessions(sinceMillis: Long): List<CompletedSleepSession> = emptyList()
}

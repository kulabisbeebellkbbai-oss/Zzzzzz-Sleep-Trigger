package com.zzzzzz.sleeptrigger.engine

enum class TriggerType {
    ASLEEP_DETECTED,
    STOOD_UP_AFTER_WAKE
}

enum class TriggerSource {
    SIMULATED,
    WEAR_HEALTH_SERVICES,
    HEALTH_CONNECT
}

enum class TaskType {
    PAUSE_MEDIA
}

enum class TaskRunStatus {
    SCHEDULED,
    RUNNING,
    SUCCEEDED,
    FAILED
}

data class TriggerEvent(
    val id: String,
    val triggerType: TriggerType,
    val source: TriggerSource,
    val detectedAtMillis: Long,
    val confidence: Float
)

data class TaskRun(
    val id: String,
    val triggerEventId: String,
    val taskType: TaskType,
    val scheduledForMillis: Long,
    val startedAtMillis: Long? = null,
    val completedAtMillis: Long? = null,
    val status: TaskRunStatus,
    val resultMessage: String? = null
)

data class LoggedTaskEvent(
    val triggerEvent: TriggerEvent,
    val taskRun: TaskRun
)


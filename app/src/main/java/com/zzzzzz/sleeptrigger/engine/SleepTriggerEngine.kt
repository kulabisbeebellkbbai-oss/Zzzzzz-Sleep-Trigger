package com.zzzzzz.sleeptrigger.engine

import com.zzzzzz.sleeptrigger.store.TaskEventRepository

class SleepTriggerEngine(
    private val eventRepository: TaskEventRepository,
    private val taskScheduler: TaskScheduler,
    private val clock: Clock = SystemClock,
    private val idFactory: IdFactory = TimestampIdFactory(clock)
) {
    fun scheduleSimulatedSleepMediaPause(delayMillis: Long): TaskRun {
        return handleTrigger(
            triggerType = TriggerType.ASLEEP_DETECTED,
            source = TriggerSource.SIMULATED,
            confidence = 1.0f,
            delayMillis = delayMillis,
            taskType = TaskType.PAUSE_MEDIA
        ).taskRun
    }

    fun handleTrigger(
        triggerType: TriggerType,
        source: TriggerSource,
        confidence: Float,
        delayMillis: Long,
        taskType: TaskType,
        metadata: Map<String, String> = emptyMap()
    ): LoggedTaskEvent {
        require(delayMillis >= 0) { "delayMillis must be non-negative" }

        val now = clock.nowMillis()
        val triggerEvent = TriggerEvent(
            id = idFactory.newId("trigger"),
            triggerType = triggerType,
            source = source,
            detectedAtMillis = now,
            confidence = confidence,
            metadata = metadata
        )
        val taskRun = TaskRun(
            id = idFactory.newId("task"),
            triggerEventId = triggerEvent.id,
            taskType = taskType,
            scheduledForMillis = now + delayMillis,
            status = TaskRunStatus.SCHEDULED
        )

        eventRepository.append(triggerEvent, taskRun)
        if (delayMillis > 0) {
            taskScheduler.schedule(
                ScheduledTaskRequest(
                    taskRunId = taskRun.id,
                    triggerEventId = triggerEvent.id,
                    taskType = taskRun.taskType,
                    scheduledForMillis = taskRun.scheduledForMillis
                )
            )
        }
        return LoggedTaskEvent(triggerEvent, taskRun)
    }
}

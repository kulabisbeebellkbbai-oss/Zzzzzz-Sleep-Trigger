package com.zzzzzz.sleeptrigger.engine

import com.zzzzzz.sleeptrigger.media.MediaPauser
import com.zzzzzz.sleeptrigger.store.TaskEventRepository

class TaskRunExecutor(
    private val eventRepository: TaskEventRepository,
    private val mediaPauser: MediaPauser,
    private val clock: Clock = SystemClock
) {
    fun execute(taskRunId: String): TaskRun? {
        val scheduledRun = eventRepository.findTaskRun(taskRunId) ?: return null
        val startedRun = scheduledRun.copy(
            startedAtMillis = clock.nowMillis(),
            status = TaskRunStatus.RUNNING
        )
        eventRepository.updateTaskRun(startedRun)

        val result = when (startedRun.taskType) {
            TaskType.PAUSE_MEDIA -> mediaPauser.pauseActiveSessions()
        }
        val completedRun = startedRun.copy(
            completedAtMillis = clock.nowMillis(),
            status = if (result.succeeded) TaskRunStatus.SUCCEEDED else TaskRunStatus.FAILED,
            resultMessage = result.message
        )
        eventRepository.updateTaskRun(completedRun)
        return completedRun
    }
}


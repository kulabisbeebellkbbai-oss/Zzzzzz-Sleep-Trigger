package com.zzzzzz.sleeptrigger.task

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.zzzzzz.sleeptrigger.engine.TaskRunStatus
import com.zzzzzz.sleeptrigger.media.MediaPauseTask
import com.zzzzzz.sleeptrigger.store.EventLogStore

class DelayedTaskReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_RUN_DELAYED_TASK) return

        val taskRunId = intent.getStringExtra(EXTRA_TASK_RUN_ID) ?: return
        val eventLogStore = EventLogStore(context)
        val scheduledRun = eventLogStore.findTaskRun(taskRunId) ?: return
        val startedRun = scheduledRun.copy(
            startedAtMillis = System.currentTimeMillis(),
            status = TaskRunStatus.RUNNING
        )
        eventLogStore.updateTaskRun(startedRun)

        val result = MediaPauseTask(context).pauseActiveSessions()
        eventLogStore.updateTaskRun(
            startedRun.copy(
                completedAtMillis = System.currentTimeMillis(),
                status = if (result.succeeded) TaskRunStatus.SUCCEEDED else TaskRunStatus.FAILED,
                resultMessage = result.message
            )
        )
    }

    companion object {
        const val ACTION_RUN_DELAYED_TASK = "com.zzzzzz.sleeptrigger.RUN_DELAYED_TASK"
        const val EXTRA_TASK_RUN_ID = "taskRunId"
        const val EXTRA_TRIGGER_EVENT_ID = "triggerEventId"
        const val EXTRA_SCHEDULED_FOR_MILLIS = "scheduledForMillis"
    }
}

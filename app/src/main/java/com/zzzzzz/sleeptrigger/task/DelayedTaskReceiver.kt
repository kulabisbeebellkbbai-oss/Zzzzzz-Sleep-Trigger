package com.zzzzzz.sleeptrigger.task

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.zzzzzz.sleeptrigger.engine.TaskRunExecutor
import com.zzzzzz.sleeptrigger.media.MediaPauseTask
import com.zzzzzz.sleeptrigger.store.EventLogStore

class DelayedTaskReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_RUN_DELAYED_TASK) return

        val taskRunId = intent.getStringExtra(EXTRA_TASK_RUN_ID) ?: return
        TaskRunExecutor(
            eventRepository = EventLogStore(context),
            mediaPauser = MediaPauseTask(context)
        ).execute(taskRunId)
    }

    companion object {
        const val ACTION_RUN_DELAYED_TASK = "com.zzzzzz.sleeptrigger.RUN_DELAYED_TASK"
        const val EXTRA_TASK_RUN_ID = "taskRunId"
        const val EXTRA_TRIGGER_EVENT_ID = "triggerEventId"
        const val EXTRA_SCHEDULED_FOR_MILLIS = "scheduledForMillis"
    }
}

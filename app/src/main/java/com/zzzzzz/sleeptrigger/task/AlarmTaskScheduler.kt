package com.zzzzzz.sleeptrigger.task

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.zzzzzz.sleeptrigger.engine.ScheduledTaskRequest
import com.zzzzzz.sleeptrigger.engine.TaskScheduler

class AlarmTaskScheduler(private val context: Context) : TaskScheduler {
    override fun schedule(request: ScheduledTaskRequest) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            request.scheduledForMillis,
            pendingIntent(request)
        )
    }

    override fun cancel(taskRunId: String) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val request = ScheduledTaskRequest(taskRunId, "", com.zzzzzz.sleeptrigger.engine.TaskType.PAUSE_MEDIA, 0)
        alarmManager.cancel(pendingIntent(request))
    }

    private fun pendingIntent(request: ScheduledTaskRequest): PendingIntent {
        val intent = Intent(context, DelayedTaskReceiver::class.java).apply {
            action = DelayedTaskReceiver.ACTION_RUN_DELAYED_TASK
            putExtra(DelayedTaskReceiver.EXTRA_TASK_RUN_ID, request.taskRunId)
            putExtra(DelayedTaskReceiver.EXTRA_TRIGGER_EVENT_ID, request.triggerEventId)
            putExtra(DelayedTaskReceiver.EXTRA_SCHEDULED_FOR_MILLIS, request.scheduledForMillis)
        }
        return PendingIntent.getBroadcast(
            context,
            request.taskRunId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}


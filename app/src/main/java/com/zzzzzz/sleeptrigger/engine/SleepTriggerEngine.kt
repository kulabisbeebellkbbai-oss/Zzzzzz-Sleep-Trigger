package com.zzzzzz.sleeptrigger.engine

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.zzzzzz.sleeptrigger.store.EventLogStore
import com.zzzzzz.sleeptrigger.task.DelayedTaskReceiver

class SleepTriggerEngine(
    private val context: Context,
    private val eventLogStore: EventLogStore = EventLogStore(context)
) {
    fun scheduleSimulatedSleepMediaPause(delayMillis: Long): TriggerEvent {
        val now = System.currentTimeMillis()
        val triggerEvent = TriggerEvent(
            id = "sleep-$now",
            triggerType = TriggerType.ASLEEP_DETECTED,
            source = TriggerSource.SIMULATED,
            detectedAtMillis = now,
            confidence = 1.0f
        )
        val taskRun = TaskRun(
            id = "task-$now",
            triggerEventId = triggerEvent.id,
            taskType = TaskType.PAUSE_MEDIA,
            scheduledForMillis = now + delayMillis,
            status = TaskRunStatus.SCHEDULED
        )

        eventLogStore.append(triggerEvent, taskRun)
        schedule(taskRun)
        return triggerEvent
    }

    private fun schedule(taskRun: TaskRun) {
        val intent = Intent(context, DelayedTaskReceiver::class.java).apply {
            action = DelayedTaskReceiver.ACTION_RUN_DELAYED_TASK
            putExtra(DelayedTaskReceiver.EXTRA_TASK_RUN_ID, taskRun.id)
            putExtra(DelayedTaskReceiver.EXTRA_TRIGGER_EVENT_ID, taskRun.triggerEventId)
            putExtra(DelayedTaskReceiver.EXTRA_SCHEDULED_FOR_MILLIS, taskRun.scheduledForMillis)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskRun.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            taskRun.scheduledForMillis,
            pendingIntent
        )
    }
}


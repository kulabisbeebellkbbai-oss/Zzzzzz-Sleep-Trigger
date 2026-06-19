package com.zzzzzz.sleeptrigger.wear

import android.content.Context
import android.util.Log
import com.zzzzzz.sleeptrigger.engine.InMemoryTriggerDefinitionRepository
import com.zzzzzz.sleeptrigger.engine.SleepTriggerEngine
import com.zzzzzz.sleeptrigger.engine.TaskRunExecutor
import com.zzzzzz.sleeptrigger.engine.TriggerRouter
import com.zzzzzz.sleeptrigger.media.MediaPauseTask
import com.zzzzzz.sleeptrigger.shared.SleepTriggerActiveWindow
import com.zzzzzz.sleeptrigger.shared.WearTriggerPayload
import com.zzzzzz.sleeptrigger.store.EventLogStore
import com.zzzzzz.sleeptrigger.task.AlarmTaskScheduler

class PhoneWearTriggerRouter(context: Context) {
    private val appContext = context.applicationContext

    fun route(payload: WearTriggerPayload) {
        if (payload.triggerType == ASLEEP_DETECTED &&
            !SleepTriggerActiveWindow.isActiveAt(payload.detectedAtMillis)
        ) {
            Log.i(
                TAG,
                "Ignoring wear sleep trigger outside active window " +
                    SleepTriggerActiveWindow.DESCRIPTION
            )
            return
        }
        Log.i(TAG, "Routing wear trigger ${payload.triggerType} from ${payload.source}")
        val repository = EventLogStore(appContext)
        val router = TriggerRouter(
            definitionRepository = InMemoryTriggerDefinitionRepository(),
            sleepTriggerEngine = SleepTriggerEngine(
                eventRepository = repository,
                taskScheduler = AlarmTaskScheduler(appContext)
            ),
            taskRunExecutor = TaskRunExecutor(
                eventRepository = repository,
                mediaPauser = MediaPauseTask(appContext)
            )
        )
        router.routeWearPayload(payload)
    }

    private companion object {
        const val TAG = "PhoneWearTriggerRouter"
        const val ASLEEP_DETECTED = "ASLEEP_DETECTED"
    }
}

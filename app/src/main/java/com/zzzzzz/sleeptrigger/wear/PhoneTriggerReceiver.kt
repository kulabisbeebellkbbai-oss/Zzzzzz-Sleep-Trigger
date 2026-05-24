package com.zzzzzz.sleeptrigger.wear

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.zzzzzz.sleeptrigger.engine.InMemoryTriggerDefinitionRepository
import com.zzzzzz.sleeptrigger.engine.SleepTriggerEngine
import com.zzzzzz.sleeptrigger.engine.TaskRunExecutor
import com.zzzzzz.sleeptrigger.engine.TriggerRouter
import com.zzzzzz.sleeptrigger.media.MediaPauseTask
import com.zzzzzz.sleeptrigger.shared.WearTriggerPayloadCodec
import com.zzzzzz.sleeptrigger.store.EventLogStore
import com.zzzzzz.sleeptrigger.task.AlarmTaskScheduler

class PhoneTriggerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_TRIGGER_FROM_WEAR) return
        val encodedPayload = intent.getStringExtra(EXTRA_PAYLOAD_JSON) ?: return
        val repository = EventLogStore(context)
        val router = TriggerRouter(
            definitionRepository = InMemoryTriggerDefinitionRepository(),
            sleepTriggerEngine = SleepTriggerEngine(
                eventRepository = repository,
                taskScheduler = AlarmTaskScheduler(context)
            ),
            taskRunExecutor = TaskRunExecutor(
                eventRepository = repository,
                mediaPauser = MediaPauseTask(context)
            )
        )
        try {
            router.routeWearPayload(WearTriggerPayloadCodec.decode(encodedPayload))
        } catch (exception: RuntimeException) {
            Log.w(TAG, "Failed to route wear trigger payload", exception)
        }
    }

    companion object {
        const val ACTION_TRIGGER_FROM_WEAR = "com.zzzzzz.sleeptrigger.TRIGGER_FROM_WEAR"
        const val EXTRA_PAYLOAD_JSON = "payloadJson"
        private const val TAG = "PhoneTriggerReceiver"
    }
}


package com.zzzzzz.sleeptrigger.wear

import android.content.Context
import android.util.Log
import com.zzzzzz.sleeptrigger.shared.WearTriggerPayload

class WearHealthTriggerSender(context: Context) {
    private val appContext = context.applicationContext
    private val transport = PhoneTriggerTransport(appContext)
    private val store = WearPassiveTriggerStateStore(appContext)

    fun send(
        triggerType: String,
        confidence: Float,
        metadata: Map<String, String>,
        source: String = "WEAR_HEALTH_SERVICES",
        surface: String = "health-services-passive"
    ) {
        val now = System.currentTimeMillis()
        val payload = WearTriggerPayload(
            eventId = "health-$now",
            triggerType = triggerType,
            source = source,
            detectedAtMillis = now,
            confidence = confidence,
            metadata = metadata + ("surface" to surface)
        )
        transport.send(payload) { result ->
            store.appendHistory(
                kind = "delivery",
                atMillis = System.currentTimeMillis(),
                values = mapOf(
                    "triggerType" to triggerType,
                    "result" to result.toString()
                )
            )
            Log.i(TAG, "Health trigger $triggerType delivery result: $result")
        }
    }

    private companion object {
        const val TAG = "WearHealthTriggerSender"
    }
}

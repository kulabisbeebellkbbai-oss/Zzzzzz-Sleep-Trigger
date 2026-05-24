package com.zzzzzz.sleeptrigger.wear

import android.content.Context
import android.util.Log
import com.zzzzzz.sleeptrigger.shared.WearTriggerPayload

class WearHealthTriggerSender(context: Context) {
    private val appContext = context.applicationContext
    private val transport = PhoneTriggerTransport(appContext)

    fun send(triggerType: String, confidence: Float, metadata: Map<String, String>) {
        val now = System.currentTimeMillis()
        val payload = WearTriggerPayload(
            eventId = "health-$now",
            triggerType = triggerType,
            source = "WEAR_HEALTH_SERVICES",
            detectedAtMillis = now,
            confidence = confidence,
            metadata = metadata + ("surface" to "health-services-passive")
        )
        transport.send(payload) { result ->
            Log.i(TAG, "Health trigger $triggerType delivery result: $result")
        }
    }

    private companion object {
        const val TAG = "WearHealthTriggerSender"
    }
}

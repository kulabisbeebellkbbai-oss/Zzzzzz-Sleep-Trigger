package com.zzzzzz.sleeptrigger.wear

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.zzzzzz.sleeptrigger.shared.WearTriggerPayloadCodec

class PhoneTriggerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_TRIGGER_FROM_WEAR) return
        val encodedPayload = intent.getStringExtra(EXTRA_PAYLOAD_JSON) ?: return
        try {
            PhoneWearTriggerRouter(context).route(WearTriggerPayloadCodec.decode(encodedPayload))
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

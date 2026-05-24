package com.zzzzzz.sleeptrigger.wear

import android.content.Context
import android.content.Intent
import com.zzzzzz.sleeptrigger.shared.WearTriggerPayload
import com.zzzzzz.sleeptrigger.shared.WearTriggerPayloadCodec

class PhoneTriggerTransport(private val context: Context) {
    fun send(payload: WearTriggerPayload) {
        val intent = Intent(ACTION_TRIGGER_FROM_WEAR).apply {
            setPackage(PHONE_PACKAGE)
            putExtra(EXTRA_PAYLOAD_JSON, WearTriggerPayloadCodec.encode(payload))
        }
        context.sendBroadcast(intent)
    }

    companion object {
        const val ACTION_TRIGGER_FROM_WEAR = "com.zzzzzz.sleeptrigger.TRIGGER_FROM_WEAR"
        const val EXTRA_PAYLOAD_JSON = "payloadJson"
        const val PERMISSION_TRIGGER_FROM_WEAR = "com.zzzzzz.sleeptrigger.permission.TRIGGER_FROM_WEAR"
        private const val PHONE_PACKAGE = "com.zzzzzz.sleeptrigger"
    }
}

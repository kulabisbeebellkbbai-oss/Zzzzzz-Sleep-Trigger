package com.zzzzzz.sleeptrigger.wear

import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import com.zzzzzz.sleeptrigger.shared.WearTriggerPayloadCodec
import com.zzzzzz.sleeptrigger.shared.WearTriggerTransportContract

class PhoneWearMessageService : WearableListenerService() {
    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.path != WearTriggerTransportContract.MESSAGE_TRIGGER_PATH) return
        try {
            val encodedPayload = messageEvent.data.toString(Charsets.UTF_8)
            PhoneWearTriggerRouter(this).route(WearTriggerPayloadCodec.decode(encodedPayload))
        } catch (exception: RuntimeException) {
            Log.w(TAG, "Failed to route wear data layer payload", exception)
        }
    }

    private companion object {
        const val TAG = "PhoneWearMessageService"
    }
}

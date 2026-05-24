package com.zzzzzz.sleeptrigger.wear

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.wearable.Wearable
import com.zzzzzz.sleeptrigger.shared.WearTriggerPayload
import com.zzzzzz.sleeptrigger.shared.WearTriggerPayloadCodec
import com.zzzzzz.sleeptrigger.shared.WearTriggerTransportContract

class PhoneTriggerTransport(private val context: Context) {
    fun send(payload: WearTriggerPayload, onResult: (TransportResult) -> Unit = {}) {
        payload.validate()
        val encodedPayload = WearTriggerPayloadCodec.encode(payload)
        sendDataLayerMessage(encodedPayload, onResult)
        sendLocalDevelopmentBroadcast(encodedPayload)
    }

    private fun sendDataLayerMessage(
        encodedPayload: String,
        onResult: (TransportResult) -> Unit
    ) {
        Wearable.getNodeClient(context).connectedNodes
            .addOnSuccessListener { nodes ->
                val targetNodes = nodes.filter { it.isNearby }.ifEmpty { nodes }
                if (targetNodes.isEmpty()) {
                    Log.w(TAG, "No connected phone nodes for trigger delivery")
                    onResult(TransportResult.NoConnectedPhone)
                    return@addOnSuccessListener
                }

                Log.i(TAG, "Sending trigger payload to ${targetNodes.size} node(s)")
                var pending = targetNodes.size
                var sent = 0
                val payloadBytes = encodedPayload.toByteArray(Charsets.UTF_8)
                targetNodes.forEach { node ->
                    Wearable.getMessageClient(context)
                        .sendMessage(
                            node.id,
                            WearTriggerTransportContract.MESSAGE_TRIGGER_PATH,
                            payloadBytes
                        )
                        .addOnSuccessListener {
                            Log.i(TAG, "Sent trigger payload to node ${node.displayName}")
                            sent += 1
                            pending -= 1
                            if (pending == 0) onResult(TransportResult.Sent(sent, targetNodes.size))
                        }
                        .addOnFailureListener { exception ->
                            Log.w(TAG, "Failed to send trigger payload to node ${node.displayName}", exception)
                            pending -= 1
                            if (pending == 0) onResult(TransportResult.Sent(sent, targetNodes.size))
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Wear transport unavailable", exception)
                onResult(TransportResult.TransportUnavailable)
            }
    }

    private fun sendLocalDevelopmentBroadcast(encodedPayload: String) {
        val intent = Intent(ACTION_TRIGGER_FROM_WEAR).apply {
            setPackage(PHONE_PACKAGE)
            putExtra(EXTRA_PAYLOAD_JSON, encodedPayload)
        }
        context.sendBroadcast(intent)
    }

    sealed class TransportResult {
        data class Sent(val deliveredCount: Int, val targetCount: Int) : TransportResult()
        data object NoConnectedPhone : TransportResult()
        data object TransportUnavailable : TransportResult()
    }

    companion object {
        const val ACTION_TRIGGER_FROM_WEAR = "com.zzzzzz.sleeptrigger.TRIGGER_FROM_WEAR"
        const val EXTRA_PAYLOAD_JSON = "payloadJson"
        const val PERMISSION_TRIGGER_FROM_WEAR = "com.zzzzzz.sleeptrigger.permission.TRIGGER_FROM_WEAR"
        private const val PHONE_PACKAGE = "com.zzzzzz.sleeptrigger"
        private const val TAG = "PhoneTriggerTransport"
    }
}

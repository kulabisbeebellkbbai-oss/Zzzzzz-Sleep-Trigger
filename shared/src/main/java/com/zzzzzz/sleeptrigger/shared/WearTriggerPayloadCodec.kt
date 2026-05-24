package com.zzzzzz.sleeptrigger.shared

import org.json.JSONObject

object WearTriggerPayloadCodec {
    fun encode(payload: WearTriggerPayload): String {
        payload.validate()
        return JSONObject()
            .put("eventId", payload.eventId)
            .put("triggerType", payload.triggerType)
            .put("source", payload.source)
            .put("detectedAtMillis", payload.detectedAtMillis)
            .put("confidence", payload.confidence.toDouble())
            .put("metadata", payload.metadata.toJson())
            .toString()
    }

    fun decode(encoded: String): WearTriggerPayload {
        val json = JSONObject(encoded)
        return WearTriggerPayload(
            eventId = json.getString("eventId"),
            triggerType = json.getString("triggerType"),
            source = json.getString("source"),
            detectedAtMillis = json.getLong("detectedAtMillis"),
            confidence = json.getDouble("confidence").toFloat(),
            metadata = json.optJSONObject("metadata")?.toStringMap().orEmpty()
        ).also { it.validate() }
    }

    private fun Map<String, String>.toJson(): JSONObject {
        val json = JSONObject()
        forEach { (key, value) -> json.put(key, value) }
        return json
    }

    private fun JSONObject.toStringMap(): Map<String, String> {
        return keys().asSequence().associateWith { key -> getString(key) }
    }
}


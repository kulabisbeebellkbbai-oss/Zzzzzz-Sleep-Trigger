package com.zzzzzz.sleeptrigger.wear

import com.zzzzzz.sleeptrigger.engine.TriggerSource
import com.zzzzzz.sleeptrigger.engine.TriggerType

data class WearTriggerPayload(
    val eventId: String,
    val triggerType: TriggerType,
    val source: TriggerSource,
    val detectedAtMillis: Long,
    val confidence: Float,
    val metadata: Map<String, String> = emptyMap()
) {
    fun validate() {
        require(eventId.isNotBlank()) { "eventId is required" }
        require(confidence in 0.0f..1.0f) { "confidence must be between 0 and 1" }
        require(detectedAtMillis > 0) { "detectedAtMillis is required" }
    }
}

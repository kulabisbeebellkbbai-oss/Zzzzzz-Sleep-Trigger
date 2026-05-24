package com.zzzzzz.sleeptrigger.shared

data class WearTriggerPayload(
    val eventId: String,
    val triggerType: String,
    val source: String,
    val detectedAtMillis: Long,
    val confidence: Float,
    val metadata: Map<String, String> = emptyMap()
) {
    fun validate() {
        require(eventId.isNotBlank()) { "eventId is required" }
        require(triggerType.isNotBlank()) { "triggerType is required" }
        require(source.isNotBlank()) { "source is required" }
        require(detectedAtMillis > 0) { "detectedAtMillis is required" }
        require(confidence in 0.0f..1.0f) { "confidence must be between 0 and 1" }
    }
}


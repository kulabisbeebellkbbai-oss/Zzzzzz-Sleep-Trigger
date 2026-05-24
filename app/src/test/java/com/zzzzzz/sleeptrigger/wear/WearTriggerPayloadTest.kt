package com.zzzzzz.sleeptrigger.wear

import com.zzzzzz.sleeptrigger.shared.WearTriggerPayload
import org.junit.Assert.assertThrows
import org.junit.Test

class WearTriggerPayloadTest {
    @Test
    fun rejectsOutOfRangeConfidence() {
        val payload = WearTriggerPayload(
            eventId = "wear-1",
            triggerType = "ASLEEP_DETECTED",
            source = "WEAR_HEALTH_SERVICES",
            detectedAtMillis = 1_000,
            confidence = 1.1f
        )

        assertThrows(IllegalArgumentException::class.java) {
            payload.validate()
        }
    }
}

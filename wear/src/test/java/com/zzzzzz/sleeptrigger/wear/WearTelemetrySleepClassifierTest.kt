package com.zzzzzz.sleeptrigger.wear

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WearTelemetrySleepClassifierTest {
    @Test
    fun staysAwakeForShortQuietPeriodsThatPreviouslyTriggered() {
        val classifier = WearTelemetrySleepClassifier()

        addQuietMinutes(classifier, 20)
        val result = classifier.evaluate(
            nowMillis = 20 * MINUTE,
            latestHeartRate = 62f,
            wornEvidence = true
        )

        assertEquals(WearTelemetrySleepClassifier.State.AWAKE_OR_UNKNOWN, result.state)
        assertEquals(20, result.quietMinutes)
    }

    @Test
    fun requiresLongQuietWindowForProbableSleep() {
        val classifier = WearTelemetrySleepClassifier()

        addQuietMinutes(classifier, 46)
        val result = classifier.evaluate(
            nowMillis = 46 * MINUTE,
            latestHeartRate = 62f,
            wornEvidence = true
        )

        assertEquals(WearTelemetrySleepClassifier.State.PROBABLE_SLEEP, result.state)
        assertTrue(result.confidence >= 0.72f)
    }

    @Test
    fun doesNotTriggerWithoutRecentWornEvidence() {
        val classifier = WearTelemetrySleepClassifier()

        addQuietMinutes(classifier, 46)
        val result = classifier.evaluate(
            nowMillis = 46 * MINUTE,
            latestHeartRate = 62f,
            wornEvidence = false
        )

        assertEquals(WearTelemetrySleepClassifier.State.AWAKE_OR_UNKNOWN, result.state)
    }

    private fun addQuietMinutes(classifier: WearTelemetrySleepClassifier, minutes: Int) {
        repeat(minutes) { minute ->
            val base = minute * MINUTE
            repeat(4) { sample ->
                classifier.addMotionSample(
                    nowMillis = base + sample * 1_000L,
                    x = 0f,
                    y = 0f,
                    z = 9.8f
                )
            }
        }
    }

    private companion object {
        const val MINUTE = 60_000L
    }
}

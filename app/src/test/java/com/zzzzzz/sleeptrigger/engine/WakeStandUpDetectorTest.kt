package com.zzzzzz.sleeptrigger.engine

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WakeStandUpDetectorTest {
    @Test
    fun ignoresMovementBeforeWake() {
        val detector = WakeStandUpDetector(debounceMillis = 30_000)

        assertFalse(detector.onMovement(1_000, 10, PostWakeActivity.WALKING))
    }

    @Test
    fun waitsForDebounceBeforeStandingTrigger() {
        val detector = WakeStandUpDetector(debounceMillis = 30_000)
        detector.onWakeDetected(1_000)

        assertFalse(detector.onMovement(10_000, 3, PostWakeActivity.WALKING))
        assertTrue(detector.onMovement(41_000, 3, PostWakeActivity.WALKING))
    }

    @Test
    fun ignoresMinorInBedMovement() {
        val detector = WakeStandUpDetector(debounceMillis = 30_000, minimumSteps = 3)
        detector.onWakeDetected(1_000)

        assertFalse(detector.onMovement(41_000, 0, PostWakeActivity.IN_BED_MOVEMENT))
    }
}


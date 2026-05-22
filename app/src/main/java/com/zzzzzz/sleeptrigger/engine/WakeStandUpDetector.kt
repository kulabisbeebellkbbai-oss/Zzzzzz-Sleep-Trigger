package com.zzzzzz.sleeptrigger.engine

class WakeStandUpDetector(
    private val monitoringWindowMillis: Long = 30 * 60 * 1000,
    private val debounceMillis: Long = 30 * 1000,
    private val minimumSteps: Int = 3
) {
    private var wakeDetectedAtMillis: Long? = null
    private var firstMovementAtMillis: Long? = null

    fun onWakeDetected(atMillis: Long) {
        wakeDetectedAtMillis = atMillis
        firstMovementAtMillis = null
    }

    fun onMovement(atMillis: Long, stepsSinceWake: Int, activity: PostWakeActivity): Boolean {
        val wakeAt = wakeDetectedAtMillis ?: return false
        if (atMillis < wakeAt || atMillis - wakeAt > monitoringWindowMillis) return false
        if (!activity.countsAsStandingEvidence && stepsSinceWake < minimumSteps) return false

        if (firstMovementAtMillis == null) firstMovementAtMillis = atMillis
        val debounced = atMillis - firstMovementAtMillis!! >= debounceMillis
        if (debounced) {
            wakeDetectedAtMillis = null
            firstMovementAtMillis = null
        }
        return debounced
    }
}

enum class PostWakeActivity {
    STILL,
    IN_BED_MOVEMENT,
    WALKING,
    STANDING
}

private val PostWakeActivity.countsAsStandingEvidence: Boolean
    get() = this == PostWakeActivity.WALKING || this == PostWakeActivity.STANDING


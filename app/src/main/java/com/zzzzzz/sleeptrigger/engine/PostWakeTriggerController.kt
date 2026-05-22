package com.zzzzzz.sleeptrigger.engine

class PostWakeTriggerController(
    private val detector: WakeStandUpDetector,
    private val triggerRouter: TriggerRouter
) {
    fun onWakeDetected(atMillis: Long) {
        detector.onWakeDetected(atMillis)
    }

    fun onMovement(
        atMillis: Long,
        stepsSinceWake: Int,
        activity: PostWakeActivity,
        source: TriggerSource = TriggerSource.PHONE_ACTIVITY_RECOGNITION
    ): List<LoggedTaskEvent> {
        val stoodUp = detector.onMovement(atMillis, stepsSinceWake, activity)
        if (!stoodUp) return emptyList()
        return triggerRouter.routeTrigger(
            triggerType = TriggerType.STOOD_UP_AFTER_WAKE,
            source = source,
            confidence = 0.7f,
            metadata = mapOf(
                "stepsSinceWake" to stepsSinceWake.toString(),
                "activity" to activity.name
            )
        )
    }
}


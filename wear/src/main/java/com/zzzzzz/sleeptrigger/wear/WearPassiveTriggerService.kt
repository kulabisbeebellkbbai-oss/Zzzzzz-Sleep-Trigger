package com.zzzzzz.sleeptrigger.wear

import android.util.Log
import androidx.health.services.client.PassiveListenerService
import androidx.health.services.client.data.UserActivityInfo
import androidx.health.services.client.data.UserActivityState

class WearPassiveTriggerService : PassiveListenerService() {
    override fun onUserActivityInfoReceived(info: UserActivityInfo) {
        val state = info.userActivityState
        val stateName = state.name
        val now = System.currentTimeMillis()
        val store = WearPassiveTriggerStateStore(this)
        val previousState = store.readLastState()
        store.writeLastState(stateName, now)
        store.appendHistory(
            kind = "activity",
            atMillis = now,
            values = mapOf(
                "state" to stateName,
                "previousState" to previousState.orEmpty()
            )
        )
        Log.i(TAG, "Passive user activity update: $stateName, previous=$previousState")

        when (state) {
            UserActivityState.USER_ACTIVITY_ASLEEP -> {
                maybeSend(store, now, "ASLEEP_DETECTED", stateName, previousState)
            }
            UserActivityState.USER_ACTIVITY_PASSIVE,
            UserActivityState.USER_ACTIVITY_EXERCISE -> {
                if (previousState == UserActivityState.USER_ACTIVITY_ASLEEP.name) {
                    maybeSend(store, now, "WAKE_DETECTED", stateName, previousState)
                    maybeSend(store, now, "STOOD_UP_AFTER_WAKE", stateName, previousState)
                }
            }
            else -> Unit
        }
    }

    override fun onPermissionLost() {
        WearPassiveTriggerStateStore(this).writeRegistrationStatus("Health Services permission lost")
        Log.w(TAG, "Health Services passive permission lost")
    }

    private fun maybeSend(
        store: WearPassiveTriggerStateStore,
        now: Long,
        triggerType: String,
        stateName: String,
        previousState: String?
    ) {
        if (!store.shouldSend(triggerType, now)) {
            store.appendHistory(
                kind = "debounced",
                atMillis = now,
                values = mapOf("triggerType" to triggerType)
            )
            Log.i(TAG, "Skipping debounced $triggerType")
            return
        }
        store.appendHistory(
            kind = "trigger",
            atMillis = now,
            values = mapOf(
                "triggerType" to triggerType,
                "state" to stateName,
                "previousState" to previousState.orEmpty()
            )
        )
        WearHealthTriggerSender(this).send(
            triggerType = triggerType,
            confidence = 0.85f,
            metadata = mapOf(
                "healthUserActivityState" to stateName,
                "previousHealthUserActivityState" to previousState.orEmpty()
            )
        )
    }

    private companion object {
        const val TAG = "WearPassiveTriggerSvc"
    }
}

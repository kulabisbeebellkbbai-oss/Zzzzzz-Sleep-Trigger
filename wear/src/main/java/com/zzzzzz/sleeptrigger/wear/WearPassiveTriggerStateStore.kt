package com.zzzzzz.sleeptrigger.wear

import android.content.Context

class WearPassiveTriggerStateStore(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    fun readLastState(): String? = preferences.getString(KEY_LAST_STATE, null)

    fun writeLastState(state: String, changedAtMillis: Long) {
        preferences.edit()
            .putString(KEY_LAST_STATE, state)
            .putLong(KEY_LAST_STATE_CHANGED_AT, changedAtMillis)
            .apply()
    }

    fun shouldSend(triggerType: String, nowMillis: Long): Boolean {
        val key = "${KEY_LAST_SENT_PREFIX}_$triggerType"
        val lastSentAt = preferences.getLong(key, 0L)
        if (nowMillis - lastSentAt < TRIGGER_DEBOUNCE_MILLIS) return false
        preferences.edit().putLong(key, nowMillis).apply()
        return true
    }

    fun writeRegistrationStatus(status: String) {
        preferences.edit()
            .putString(KEY_REGISTRATION_STATUS, status)
            .putLong(KEY_REGISTRATION_STATUS_AT, System.currentTimeMillis())
            .apply()
    }

    fun readRegistrationStatus(): String {
        val status = preferences.getString(KEY_REGISTRATION_STATUS, null) ?: "Not registered"
        val at = preferences.getLong(KEY_REGISTRATION_STATUS_AT, 0L)
        return if (at > 0L) "$status\n$at" else status
    }

    private companion object {
        const val NAME = "wear-passive-triggers"
        const val KEY_LAST_STATE = "lastState"
        const val KEY_LAST_STATE_CHANGED_AT = "lastStateChangedAt"
        const val KEY_LAST_SENT_PREFIX = "lastSent"
        const val KEY_REGISTRATION_STATUS = "registrationStatus"
        const val KEY_REGISTRATION_STATUS_AT = "registrationStatusAt"
        const val TRIGGER_DEBOUNCE_MILLIS = 10 * 60 * 1000L
    }
}

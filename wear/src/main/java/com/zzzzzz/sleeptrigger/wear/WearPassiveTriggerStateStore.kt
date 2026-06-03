package com.zzzzzz.sleeptrigger.wear

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class WearPassiveTriggerStateStore(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    fun readLastState(): String? = preferences.getString(KEY_LAST_STATE, null)

    fun writeLastState(state: String, changedAtMillis: Long) {
        preferences.edit()
            .putString(KEY_LAST_STATE, state)
            .putLong(KEY_LAST_STATE_CHANGED_AT, changedAtMillis)
            .apply()
    }

    fun appendHistory(kind: String, atMillis: Long, values: Map<String, String> = emptyMap()) {
        val history = JSONArray(preferences.getString(KEY_HISTORY, "[]"))
        history.put(
            JSONObject().apply {
                put("kind", kind)
                put("atMillis", atMillis)
                values.forEach { (key, value) -> put(key, value) }
            }
        )
        while (history.length() > MAX_HISTORY_ENTRIES) {
            history.remove(0)
        }
        preferences.edit().putString(KEY_HISTORY, history.toString()).apply()
    }

    fun readHistorySummary(): String {
        val history = JSONArray(preferences.getString(KEY_HISTORY, "[]"))
        if (history.length() == 0) return "No passive history"
        val first = history.getJSONObject(0)
        val last = history.getJSONObject(history.length() - 1)
        return "${history.length()} passive records\n" +
            "first=${first.optString("kind")}@${first.optLong("atMillis")}\n" +
            "last=${last.optString("kind")}@${last.optLong("atMillis")}\n" +
            readTelemetryStatus()
    }

    fun shouldSend(triggerType: String, nowMillis: Long): Boolean {
        val key = "${KEY_LAST_SENT_PREFIX}_$triggerType"
        val lastSentAt = preferences.getLong(key, 0L)
        if (nowMillis - lastSentAt < TRIGGER_DEBOUNCE_MILLIS) return false
        preferences.edit().putLong(key, nowMillis).apply()
        return true
    }

    fun writeRegistrationStatus(status: String) {
        val now = System.currentTimeMillis()
        preferences.edit()
            .putString(KEY_REGISTRATION_STATUS, status)
            .putLong(KEY_REGISTRATION_STATUS_AT, now)
            .apply()
        appendHistory("registration", now, mapOf("status" to status))
    }

    fun writeTelemetryStatus(status: String) {
        val now = System.currentTimeMillis()
        preferences.edit()
            .putString(KEY_TELEMETRY_STATUS, status)
            .putLong(KEY_TELEMETRY_STATUS_AT, now)
            .apply()
        appendHistory("telemetryStatus", now, mapOf("status" to status))
    }

    fun readTelemetryStatus(): String {
        val status = preferences.getString(KEY_TELEMETRY_STATUS, null) ?: "Telemetry not running"
        val at = preferences.getLong(KEY_TELEMETRY_STATUS_AT, 0L)
        return if (at > 0L) "$status\n$at" else status
    }

    fun shouldSendTelemetrySleep(nowMillis: Long): Boolean {
        val lastSentAt = preferences.getLong(KEY_LAST_TELEMETRY_SLEEP_SENT_AT, 0L)
        if (nowMillis - lastSentAt < TELEMETRY_SLEEP_DEBOUNCE_MILLIS) return false
        preferences.edit().putLong(KEY_LAST_TELEMETRY_SLEEP_SENT_AT, nowMillis).apply()
        return true
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
        const val KEY_HISTORY = "history"
        const val KEY_TELEMETRY_STATUS = "telemetryStatus"
        const val KEY_TELEMETRY_STATUS_AT = "telemetryStatusAt"
        const val KEY_LAST_TELEMETRY_SLEEP_SENT_AT = "lastTelemetrySleepSentAt"
        const val TRIGGER_DEBOUNCE_MILLIS = 10 * 60 * 1000L
        const val TELEMETRY_SLEEP_DEBOUNCE_MILLIS = 18 * 60 * 60 * 1000L
        const val MAX_HISTORY_ENTRIES = 200
    }
}

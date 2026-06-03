package com.zzzzzz.sleeptrigger.sleep

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.SleepClassifyEvent
import com.google.android.gms.location.SleepSegmentEvent
import com.google.android.gms.location.SleepSegmentRequest
import com.zzzzzz.sleeptrigger.engine.InMemoryTriggerDefinitionRepository
import com.zzzzzz.sleeptrigger.engine.SleepTriggerEngine
import com.zzzzzz.sleeptrigger.engine.TriggerRouter
import com.zzzzzz.sleeptrigger.engine.TriggerSource
import com.zzzzzz.sleeptrigger.engine.TriggerType
import com.zzzzzz.sleeptrigger.store.EventLogStore
import com.zzzzzz.sleeptrigger.task.AlarmTaskScheduler

class PhoneSleepApiRegistrar(private val context: Context) {
    private val appContext = context.applicationContext
    private val statusStore = PhoneSleepApiStatusStore(appContext)

    fun register() {
        if (!activityRecognitionGranted()) {
            statusStore.write("Sleep API registration skipped: activity recognition needs access")
            return
        }

        ActivityRecognition.getClient(appContext)
            .requestSleepSegmentUpdates(callbackIntent(), SleepSegmentRequest.getDefaultSleepSegmentRequest())
            .addOnSuccessListener {
                statusStore.write("Sleep API registered")
            }
            .addOnFailureListener { error ->
                statusStore.write("Sleep API registration failed: ${error.javaClass.simpleName}")
                Log.w(TAG, "Sleep API registration failed", error)
            }
    }

    fun readStatus(): String = statusStore.read()

    private fun activityRecognitionGranted(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return true
        return appContext.checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION) ==
            PackageManager.PERMISSION_GRANTED
    }

    private fun callbackIntent(): PendingIntent {
        val intent = Intent(appContext, PhoneSleepApiReceiver::class.java)
            .setAction(PhoneSleepApiReceiver.ACTION_SLEEP_API_EVENT)
        return PendingIntent.getBroadcast(
            appContext,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    private companion object {
        const val REQUEST_CODE = 4001
        const val TAG = "PhoneSleepApiRegistrar"
    }
}

class PhoneSleepApiStatusStore(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    fun read(): String = preferences.getString(KEY_STATUS, "Sleep API: not registered").orEmpty()

    fun write(status: String) {
        preferences.edit().putString(KEY_STATUS, status).apply()
    }

    fun shouldRouteClassifySleep(atMillis: Long): Boolean {
        val lastSentAt = preferences.getLong(KEY_LAST_CLASSIFY_SLEEP_SENT_AT, 0L)
        if (atMillis - lastSentAt < CLASSIFY_TRIGGER_DEBOUNCE_MILLIS) return false

        val lastCandidateAt = preferences.getLong(KEY_LAST_CLASSIFY_CANDIDATE_AT, 0L)
        val previousCandidateCount = preferences.getInt(KEY_CLASSIFY_CANDIDATE_COUNT, 0)
        val candidateCount = if (atMillis - lastCandidateAt <= CLASSIFY_CONFIRMATION_WINDOW_MILLIS) {
            previousCandidateCount + 1
        } else {
            1
        }
        preferences.edit()
            .putLong(KEY_LAST_CLASSIFY_CANDIDATE_AT, atMillis)
            .putInt(KEY_CLASSIFY_CANDIDATE_COUNT, candidateCount)
            .apply()
        return candidateCount >= REQUIRED_CLASSIFY_CANDIDATES
    }

    fun markClassifySleepRouted(atMillis: Long) {
        preferences.edit()
            .putLong(KEY_LAST_CLASSIFY_SLEEP_SENT_AT, atMillis)
            .putInt(KEY_CLASSIFY_CANDIDATE_COUNT, 0)
            .apply()
    }

    private companion object {
        const val NAME = "phone-sleep-api-status"
        const val KEY_STATUS = "status"
        const val KEY_LAST_CLASSIFY_SLEEP_SENT_AT = "lastClassifySleepSentAt"
        const val KEY_LAST_CLASSIFY_CANDIDATE_AT = "lastClassifyCandidateAt"
        const val KEY_CLASSIFY_CANDIDATE_COUNT = "classifyCandidateCount"
        const val CLASSIFY_TRIGGER_DEBOUNCE_MILLIS = 18 * 60 * 60 * 1000L
        const val CLASSIFY_CONFIRMATION_WINDOW_MILLIS = 60 * 60 * 1000L
        const val REQUIRED_CLASSIFY_CANDIDATES = 3
    }
}

class PhoneSleepApiReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                PhoneSleepApiRegistrar(context).register()
                return
            }
            ACTION_SLEEP_API_EVENT -> Unit
            else -> return
        }
        val statusStore = PhoneSleepApiStatusStore(context)
        var segments = 0
        var classifications = 0

        if (SleepSegmentEvent.hasEvents(intent)) {
            SleepSegmentEvent.extractEvents(intent).forEach { event ->
                segments += 1
                recordSleepSegment(statusStore, event)
            }
        }

        if (SleepClassifyEvent.hasEvents(intent)) {
            SleepClassifyEvent.extractEvents(intent).forEach { event ->
                classifications += 1
                routeHighConfidenceClassify(context, statusStore, event)
            }
        }

        statusStore.write("Sleep API callback: $segments segments, $classifications classifications")
    }

    private fun routeHighConfidenceClassify(
        context: Context,
        statusStore: PhoneSleepApiStatusStore,
        event: SleepClassifyEvent
    ) {
        if (event.confidence < CLASSIFY_SLEEP_CONFIDENCE_THRESHOLD) return
        if (event.motion > CLASSIFY_MAX_MOTION) return
        if (event.light > CLASSIFY_MAX_LIGHT) return
        if (!statusStore.shouldRouteClassifySleep(event.timestampMillis)) return
        routeAsleepDetected(
            context = context,
            confidence = event.confidence / 100f,
            metadata = mapOf(
                "sleepApiEventType" to "classify",
                "sleepConfidence" to event.confidence.toString(),
                "sleepTimestampMillis" to event.timestampMillis.toString(),
                "sleepLight" to event.light.toString(),
                "sleepMotion" to event.motion.toString()
            )
        )
        statusStore.markClassifySleepRouted(event.timestampMillis)
    }

    private fun recordSleepSegment(statusStore: PhoneSleepApiStatusStore, event: SleepSegmentEvent) {
        if (event.status != SleepSegmentEvent.STATUS_SUCCESSFUL &&
            event.status != SleepSegmentEvent.STATUS_MISSING_DATA
        ) {
            statusStore.write("Sleep API segment status: ${event.status}")
            return
        }
        statusStore.write(
            "Sleep API segment observed: status=${event.status} " +
                "start=${event.startTimeMillis} end=${event.endTimeMillis}"
        )
    }

    private fun routeAsleepDetected(context: Context, confidence: Float, metadata: Map<String, String>) {
        val repository = EventLogStore(context)
        TriggerRouter(
            definitionRepository = InMemoryTriggerDefinitionRepository(),
            sleepTriggerEngine = SleepTriggerEngine(
                eventRepository = repository,
                taskScheduler = AlarmTaskScheduler(context)
            )
        ).routeTrigger(
            triggerType = TriggerType.ASLEEP_DETECTED,
            source = TriggerSource.PHONE_ACTIVITY_RECOGNITION,
            confidence = confidence,
            metadata = metadata
        )
    }

    companion object {
        const val ACTION_SLEEP_API_EVENT = "com.zzzzzz.sleeptrigger.sleep.SLEEP_API_EVENT"
        const val ACTION_REGISTER_SLEEP_API = "com.zzzzzz.sleeptrigger.sleep.REGISTER_SLEEP_API"
        const val CLASSIFY_SLEEP_CONFIDENCE_THRESHOLD = 95
        const val CLASSIFY_MAX_MOTION = 2
        const val CLASSIFY_MAX_LIGHT = 2
    }
}

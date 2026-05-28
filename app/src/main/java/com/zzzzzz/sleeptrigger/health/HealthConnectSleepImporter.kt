package com.zzzzzz.sleeptrigger.health

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.zzzzzz.sleeptrigger.engine.InMemoryTriggerDefinitionRepository
import com.zzzzzz.sleeptrigger.engine.SleepTriggerEngine
import com.zzzzzz.sleeptrigger.store.EventLogStore
import com.zzzzzz.sleeptrigger.task.AlarmTaskScheduler
import com.zzzzzz.sleeptrigger.engine.TriggerRouter
import com.zzzzzz.sleeptrigger.engine.TriggerSource
import com.zzzzzz.sleeptrigger.engine.TriggerType
import kotlinx.coroutines.runBlocking
import java.time.Instant

data class CompletedSleepSession(
    val id: String,
    val startMillis: Long,
    val endMillis: Long,
    val sourcePackage: String
)

interface HealthConnectSleepImporter {
    fun importCompletedSleepSessions(sinceMillis: Long): List<CompletedSleepSession>
}

class NoopHealthConnectSleepImporter : HealthConnectSleepImporter {
    override fun importCompletedSleepSessions(sinceMillis: Long): List<CompletedSleepSession> = emptyList()
}

class AndroidHealthConnectSleepImporter(
    context: Context,
    private val nowMillis: () -> Long = { System.currentTimeMillis() }
) : HealthConnectSleepImporter {
    private val appContext = context.applicationContext

    override fun importCompletedSleepSessions(sinceMillis: Long): List<CompletedSleepSession> = runBlocking {
        if (HealthConnectClient.getSdkStatus(appContext) != HealthConnectClient.SDK_AVAILABLE) {
            return@runBlocking emptyList()
        }

        val client = HealthConnectClient.getOrCreate(appContext)
        val sleepPermission = HealthPermission.getReadPermission(SleepSessionRecord::class)
        if (!client.permissionController.getGrantedPermissions().contains(sleepPermission)) {
            return@runBlocking emptyList()
        }

        val response = client.readRecords(
            ReadRecordsRequest<SleepSessionRecord>(
                timeRangeFilter = TimeRangeFilter.between(
                    Instant.ofEpochMilli(sinceMillis),
                    Instant.ofEpochMilli(nowMillis())
                )
            )
        )

        response.records
            .filter { record -> record.endTime.toEpochMilli() >= sinceMillis }
            .map { record ->
                val sourcePackage = record.metadata.dataOrigin.packageName
                CompletedSleepSession(
                    id = record.metadata.id.ifBlank {
                        "${record.startTime.toEpochMilli()}-${record.endTime.toEpochMilli()}-$sourcePackage"
                    },
                    startMillis = record.startTime.toEpochMilli(),
                    endMillis = record.endTime.toEpochMilli(),
                    sourcePackage = sourcePackage
                )
            }
            .sortedBy { session -> session.endMillis }
    }
}

interface ImportedSleepSessionStore {
    fun hasImported(sessionId: String): Boolean
    fun markImported(sessionId: String)
}

class SharedPreferencesImportedSleepSessionStore(context: Context) : ImportedSleepSessionStore {
    private val preferences = context.applicationContext.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    override fun hasImported(sessionId: String): Boolean {
        return preferences.getBoolean(sessionId.toPreferenceKey(), false)
    }

    override fun markImported(sessionId: String) {
        preferences.edit().putBoolean(sessionId.toPreferenceKey(), true).apply()
    }

    private fun String.toPreferenceKey(): String = "imported_$this"

    private companion object {
        const val NAME = "health-connect-sleep-imports"
    }
}

data class SleepImportResult(
    val importedSessions: Int,
    val routedEvents: Int
)

class SleepSessionTriggerImporter(
    private val sleepImporter: HealthConnectSleepImporter,
    private val importedStore: ImportedSleepSessionStore,
    private val triggerRouter: TriggerRouter
) {
    fun importAndRoute(sinceMillis: Long): SleepImportResult {
        var importedSessions = 0
        var routedEvents = 0
        sleepImporter.importCompletedSleepSessions(sinceMillis)
            .filterNot { session -> importedStore.hasImported(session.id) }
            .forEach { session ->
                val routed = triggerRouter.routeTrigger(
                    triggerType = TriggerType.ASLEEP_DETECTED,
                    source = TriggerSource.HEALTH_CONNECT,
                    confidence = 0.75f,
                    metadata = mapOf(
                        "healthConnectSleepSessionId" to session.id,
                        "sleepStartMillis" to session.startMillis.toString(),
                        "sleepEndMillis" to session.endMillis.toString(),
                        "sourcePackage" to session.sourcePackage
                    )
                )
                if (routed.isNotEmpty()) {
                    importedSessions += 1
                    routedEvents += routed.size
                    importedStore.markImported(session.id)
                }
            }
        return SleepImportResult(importedSessions, routedEvents)
    }
}

class HealthConnectSleepImportController(private val context: Context) {
    private val appContext = context.applicationContext
    private val statusStore = HealthConnectSleepImportStatusStore(appContext)

    fun importLast48Hours(): String {
        val status = healthConnectStatus()
        if (status != STATUS_READY) {
            return "Sleep import skipped: Health Connect sleep access $status"
                .also(statusStore::write)
        }

        val eventLogStore = EventLogStore(appContext)
        val result = SleepSessionTriggerImporter(
            sleepImporter = AndroidHealthConnectSleepImporter(appContext),
            importedStore = SharedPreferencesImportedSleepSessionStore(appContext),
            triggerRouter = TriggerRouter(
                definitionRepository = InMemoryTriggerDefinitionRepository(),
                sleepTriggerEngine = SleepTriggerEngine(
                    eventRepository = eventLogStore,
                    taskScheduler = AlarmTaskScheduler(appContext)
                )
            )
        ).importAndRoute(System.currentTimeMillis() - LOOKBACK_MILLIS)

        return "Sleep import: ${result.importedSessions} sessions, ${result.routedEvents} routed events"
            .also(statusStore::write)
    }

    fun readStatus(): String = statusStore.read()

    fun healthConnectStatus(): String {
        if (HealthConnectClient.getSdkStatus(appContext) != HealthConnectClient.SDK_AVAILABLE) {
            return "not available"
        }
        val granted = runBlocking {
            HealthConnectClient.getOrCreate(appContext)
                .permissionController
                .getGrantedPermissions()
        }
        return if (granted.contains(SLEEP_READ_PERMISSION)) STATUS_READY else "needs access"
    }

    companion object {
        const val ACTION_IMPORT_SLEEP = "com.zzzzzz.sleeptrigger.health.IMPORT_SLEEP"
        const val STATUS_READY = "ready"
        const val LOOKBACK_MILLIS = 48 * 60 * 60 * 1000L
        val SLEEP_READ_PERMISSION = HealthPermission.getReadPermission(SleepSessionRecord::class)
    }
}

class HealthConnectSleepImportStatusStore(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    fun read(): String = preferences.getString(KEY_LAST_STATUS, "Sleep import: not run").orEmpty()

    fun write(status: String) {
        preferences.edit().putString(KEY_LAST_STATUS, status).apply()
    }

    private companion object {
        const val NAME = "health-connect-import-status"
        const val KEY_LAST_STATUS = "lastStatus"
    }
}

class HealthConnectSleepImportReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != HealthConnectSleepImportController.ACTION_IMPORT_SLEEP) return
        val status = HealthConnectSleepImportController(context).importLast48Hours()
        Log.i(TAG, status)
    }

    private companion object {
        const val TAG = "HealthConnectImport"
    }
}

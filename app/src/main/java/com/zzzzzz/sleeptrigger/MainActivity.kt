package com.zzzzzz.sleeptrigger

import android.content.Intent
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.SleepSessionRecord
import com.zzzzzz.sleeptrigger.engine.InMemoryTriggerDefinitionRepository
import com.zzzzzz.sleeptrigger.engine.SleepTriggerEngine
import com.zzzzzz.sleeptrigger.engine.TaskRunExecutor
import com.zzzzzz.sleeptrigger.engine.TaskRunStatus
import com.zzzzzz.sleeptrigger.engine.TriggerRouter
import com.zzzzzz.sleeptrigger.engine.TriggerSource
import com.zzzzzz.sleeptrigger.engine.TriggerType
import com.zzzzzz.sleeptrigger.health.HealthConnectSleepImportController
import com.zzzzzz.sleeptrigger.media.MediaPauseTask
import com.zzzzzz.sleeptrigger.permissions.AndroidPermissionStatusReader
import com.zzzzzz.sleeptrigger.permissions.PermissionStatus
import com.zzzzzz.sleeptrigger.store.EventLogStore
import com.zzzzzz.sleeptrigger.task.AlarmTaskScheduler
import java.text.DateFormat
import java.util.Date

class MainActivity : ComponentActivity() {
    private lateinit var permissionSummary: TextView
    private lateinit var eventSummary: TextView
    private lateinit var healthImportSummary: TextView
    private lateinit var mediaPermissionStatus: TextView
    private lateinit var notificationPermissionStatus: TextView
    private lateinit var activityPermissionStatus: TextView
    private lateinit var healthConnectStatus: TextView
    private lateinit var healthPermissionLauncher: ActivityResultLauncher<Set<String>>
    private lateinit var eventLogStore: EventLogStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        healthPermissionLauncher = registerForActivityResult(
            PermissionController.createRequestPermissionResultContract()
        ) {
            renderStatus()
        }
        eventLogStore = EventLogStore(this)
        setContentView(buildContent())
        renderStatus()
    }

    override fun onResume() {
        super.onResume()
        renderStatus()
    }

    private fun buildContent(): View {
        return ScrollView(this).apply {
            setBackgroundColor(COLOR_BACKGROUND)
            addView(
                LinearLayout(this@MainActivity).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(32, 28, 32, 40)
                    addView(heroSection())
                    addView(automationSection())
                    addView(permissionSection())
                    addView(eventSection())
                }
            )
        }
    }

    private fun heroSection(): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(
                TextView(this@MainActivity).apply {
                    text = "Zzzzzz"
                    textSize = 32f
                    setTypeface(Typeface.DEFAULT, Typeface.BOLD)
                    setTextColor(COLOR_TEXT)
                }
            )
            addView(
                TextView(this@MainActivity).apply {
                    text = "Sleep-triggered automation"
                    textSize = 16f
                    setTextColor(COLOR_MUTED)
                    setPadding(0, 4, 0, 18)
                }
            )
            permissionSummary = TextView(this@MainActivity).apply {
                textSize = 14f
                setTextColor(COLOR_TEXT)
                setPadding(22, 18, 22, 18)
                background = rounded(COLOR_PANEL, COLOR_BORDER)
            }
            addView(permissionSummary, matchWrap())
        }
    }

    private fun automationSection(): View {
        return section("Automations").apply {
            addView(
                automationRow(
                    title = "Sleep detected",
                    detail = "Pause active media after 5 minutes.",
                    buttonText = "Run 5 min",
                    onClick = {
                        sleepTriggerEngine().scheduleSimulatedSleepMediaPause(5 * 60 * 1000)
                        renderStatus()
                    }
                )
            )
            addView(
                automationRow(
                    title = "Quick test",
                    detail = "Use a 10 second delay for local verification.",
                    buttonText = "Run 10 sec",
                    onClick = {
                        sleepTriggerEngine().scheduleSimulatedSleepMediaPause(10_000)
                        renderStatus()
                    }
                )
            )
            addView(
                automationRow(
                    title = "Completed sleep import",
                    detail = "Read Health Connect sleep sessions from the last 48 hours.",
                    buttonText = "Import",
                    onClick = {
                        importCompletedSleepSessions()
                        renderStatus()
                    }
                )
            )
            healthImportSummary = TextView(this@MainActivity).apply {
                text = "Sleep import: not run"
                textSize = 13f
                setTextColor(COLOR_MUTED)
                setPadding(18, 8, 18, 2)
            }
            addView(healthImportSummary)
            addView(
                automationRow(
                    title = "Stood up after wake",
                    detail = "Fire the wake movement trigger route.",
                    buttonText = "Trigger",
                    onClick = {
                        TriggerRouter(
                            definitionRepository = InMemoryTriggerDefinitionRepository(),
                            sleepTriggerEngine = sleepTriggerEngine(),
                            taskRunExecutor = TaskRunExecutor(
                                eventRepository = eventLogStore,
                                mediaPauser = MediaPauseTask(this@MainActivity)
                            )
                        ).routeTrigger(
                            triggerType = TriggerType.STOOD_UP_AFTER_WAKE,
                            source = TriggerSource.SIMULATED,
                            confidence = 1.0f
                        )
                        renderStatus()
                    }
                )
            )
        }
    }

    private fun permissionSection(): View {
        return section("Access").apply {
            mediaPermissionStatus = permissionRow("Media control")
            notificationPermissionStatus = permissionRow("Notifications")
            activityPermissionStatus = permissionRow("Activity recognition")
            healthConnectStatus = permissionRow("Health Connect sleep")
            addView(mediaPermissionStatus)
            addView(notificationPermissionStatus)
            addView(activityPermissionStatus)
            addView(healthConnectStatus)
            addView(
                primaryButton("Open notification access") {
                    startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                },
                matchWrap(top = 14)
            )
            addView(
                primaryButton("Grant sleep access") {
                    healthPermissionLauncher.launch(setOf(SLEEP_READ_PERMISSION))
                },
                matchWrap(top = 8)
            )
        }
    }

    private fun eventSection(): View {
        return section("Recent events").apply {
            eventSummary = TextView(this@MainActivity).apply {
                textSize = 14f
                setTextColor(COLOR_TEXT)
                setLineSpacing(2f, 1.0f)
            }
            addView(eventSummary)
        }
    }

    private fun section(title: String): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 24, 0, 0)
            addView(
                TextView(this@MainActivity).apply {
                    text = title
                    textSize = 20f
                    setTypeface(Typeface.DEFAULT, Typeface.BOLD)
                    setTextColor(COLOR_TEXT)
                    setPadding(0, 0, 0, 10)
                }
            )
        }
    }

    private fun automationRow(title: String, detail: String, buttonText: String, onClick: () -> Unit): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(18, 16, 18, 16)
            background = rounded(COLOR_PANEL, COLOR_BORDER)

            addView(
                LinearLayout(this@MainActivity).apply {
                    orientation = LinearLayout.VERTICAL
                    addView(label(title, 16f, COLOR_TEXT, bold = true))
                    addView(label(detail, 13f, COLOR_MUTED))
                },
                LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            )
            addView(primaryButton(buttonText, onClick), LinearLayout.LayoutParams(128, 48))
        }.also { it.layoutParams = matchWrap(top = 8) }
    }

    private fun permissionRow(label: String): TextView {
        return TextView(this).apply {
            textSize = 14f
            setTextColor(COLOR_TEXT)
            setPadding(18, 12, 18, 12)
            background = rounded(COLOR_PANEL, COLOR_BORDER)
            layoutParams = matchWrap(top = 8)
            text = "$label: unknown"
        }
    }

    private fun renderStatus() {
        val permissions = AndroidPermissionStatusReader(this).read()
        renderPermissions(permissions)
        healthImportSummary.text = HealthConnectSleepImportController(this).readStatus()
        renderEvents()
    }

    private fun renderPermissions(permissions: PermissionStatus) {
        val readyCount = listOf(
            permissions.notificationListenerEnabled,
            permissions.notificationsEnabled,
            permissions.activityRecognitionGranted
        ).count { it }
        val mediaMessage = if (permissions.notificationListenerEnabled) {
            "Media pause can control active playback sessions."
        } else {
            "Media pause needs notification listener access before real playback can be controlled."
        }
        permissionSummary.text = "$readyCount of 3 access checks ready\n" +
            mediaMessage
        mediaPermissionStatus.text = "Media control: ${statusWord(permissions.notificationListenerEnabled)}"
        notificationPermissionStatus.text = "Notifications: ${statusWord(permissions.notificationsEnabled)}"
        activityPermissionStatus.text = "Activity recognition: ${statusWord(permissions.activityRecognitionGranted)}"
        healthConnectStatus.text = "Health Connect sleep: ${healthConnectStatusWord()}"
    }

    private fun renderEvents() {
        val events = eventLogStore.readAll().takeLast(5).asReversed()
        eventSummary.text = if (events.isEmpty()) {
            getString(R.string.app_status)
        } else {
            events.joinToString(separator = "\n\n") { event ->
                val detectedAt = DateFormat.getDateTimeInstance().format(Date(event.triggerEvent.detectedAtMillis))
                val scheduledFor = DateFormat.getTimeInstance().format(Date(event.taskRun.scheduledForMillis))
                val state = when (event.taskRun.status) {
                    TaskRunStatus.SCHEDULED -> "Scheduled"
                    TaskRunStatus.RUNNING -> "Running"
                    TaskRunStatus.SUCCEEDED -> "Done"
                    TaskRunStatus.FAILED -> "Failed"
                }
                "$state: ${event.triggerEvent.triggerType}\n" +
                    "$detectedAt -> $scheduledFor\n" +
                    event.taskRun.resultMessage.orEmpty()
            }
        }
    }

    private fun sleepTriggerEngine(): SleepTriggerEngine {
        return SleepTriggerEngine(
            eventRepository = eventLogStore,
            taskScheduler = AlarmTaskScheduler(this)
        )
    }

    private fun importCompletedSleepSessions() {
        healthImportSummary.text = HealthConnectSleepImportController(this).importLast48Hours()
    }

    private fun healthConnectStatusWord(): String {
        return HealthConnectSleepImportController(this).healthConnectStatus()
    }

    private fun statusWord(enabled: Boolean): String = if (enabled) "ready" else "needs access"

    private fun label(textValue: String, size: Float, color: Int, bold: Boolean = false): TextView {
        return TextView(this).apply {
            text = textValue
            textSize = size
            setTextColor(color)
            if (bold) setTypeface(Typeface.DEFAULT, Typeface.BOLD)
        }
    }

    private fun primaryButton(textValue: String, onClick: () -> Unit): Button {
        return Button(this).apply {
            text = textValue
            textSize = 13f
            setTextColor(0xFFFFFFFF.toInt())
            background = rounded(COLOR_ACTION, COLOR_ACTION)
            setOnClickListener { onClick() }
            isAllCaps = false
        }
    }

    private fun rounded(fill: Int, stroke: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 14f
            setColor(fill)
            setStroke(1, stroke)
        }
    }

    private fun matchWrap(top: Int = 0): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = top
        }
    }

    private companion object {
        const val COLOR_BACKGROUND = 0xFFF5F7FA.toInt()
        const val COLOR_PANEL = 0xFFFFFFFF.toInt()
        const val COLOR_TEXT = 0xFF17202A.toInt()
        const val COLOR_MUTED = 0xFF5C6875.toInt()
        const val COLOR_BORDER = 0xFFE1E6ED.toInt()
        const val COLOR_ACTION = 0xFF176B5B.toInt()
        val SLEEP_READ_PERMISSION = HealthPermission.getReadPermission(SleepSessionRecord::class)
    }
}

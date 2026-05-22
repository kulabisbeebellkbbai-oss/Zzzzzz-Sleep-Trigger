package com.zzzzzz.sleeptrigger

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.zzzzzz.sleeptrigger.engine.InMemoryTriggerDefinitionRepository
import com.zzzzzz.sleeptrigger.engine.SleepTriggerEngine
import com.zzzzzz.sleeptrigger.engine.TriggerRouter
import com.zzzzzz.sleeptrigger.engine.TriggerSource
import com.zzzzzz.sleeptrigger.engine.TriggerType
import com.zzzzzz.sleeptrigger.permissions.AndroidPermissionStatusReader
import com.zzzzzz.sleeptrigger.store.EventLogStore
import com.zzzzzz.sleeptrigger.task.AlarmTaskScheduler
import java.text.DateFormat
import java.util.Date

class MainActivity : Activity() {
    private lateinit var statusText: TextView
    private lateinit var permissionText: TextView
    private lateinit var eventLogStore: EventLogStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        eventLogStore = EventLogStore(this)

        statusText = TextView(this).apply {
            textSize = 16f
            setPadding(0, 0, 0, 24)
        }
        permissionText = TextView(this).apply {
            textSize = 14f
            setPadding(0, 0, 0, 24)
        }

        val testTriggerButton = Button(this).apply {
            text = "Sleep + 10 sec"
            setOnClickListener {
                sleepTriggerEngine().scheduleSimulatedSleepMediaPause(10_000)
                renderStatus()
            }
        }
        val fiveMinuteTriggerButton = Button(this).apply {
            text = "Sleep + 5 min"
            setOnClickListener {
                sleepTriggerEngine().scheduleSimulatedSleepMediaPause(5 * 60 * 1000)
                renderStatus()
            }
        }
        val stoodUpTriggerButton = Button(this).apply {
            text = "Stood up after wake"
            setOnClickListener {
                TriggerRouter(
                    definitionRepository = InMemoryTriggerDefinitionRepository(),
                    sleepTriggerEngine = sleepTriggerEngine()
                ).routeTrigger(
                    triggerType = TriggerType.STOOD_UP_AFTER_WAKE,
                    source = TriggerSource.SIMULATED,
                    confidence = 1.0f
                )
                renderStatus()
            }
        }
        val notificationAccessButton = Button(this).apply {
            text = "Notification access"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            }
        }

        setContentView(
            LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(32, 32, 32, 32)
                addView(permissionText)
                addView(statusText)
                addView(testTriggerButton, buttonLayoutParams())
                addView(fiveMinuteTriggerButton, buttonLayoutParams())
                addView(stoodUpTriggerButton, buttonLayoutParams())
                addView(notificationAccessButton, buttonLayoutParams())
            }
        )
        renderStatus()
    }

    override fun onResume() {
        super.onResume()
        renderStatus()
    }

    private fun renderStatus() {
        val permissions = AndroidPermissionStatusReader(this).read()
        permissionText.text = "Media access: ${permissions.notificationListenerEnabled}\n" +
            "Notifications: ${permissions.notificationsEnabled}\n" +
            "Activity recognition: ${permissions.activityRecognitionGranted}"

        val events = eventLogStore.readAll().takeLast(5).asReversed()
        statusText.text = if (events.isEmpty()) {
            getString(R.string.app_status)
        } else {
            events.joinToString(separator = "\n\n") { event ->
                val detectedAt = DateFormat.getDateTimeInstance().format(Date(event.triggerEvent.detectedAtMillis))
                val scheduledFor = DateFormat.getTimeInstance().format(Date(event.taskRun.scheduledForMillis))
                "$detectedAt\n${event.triggerEvent.triggerType} -> ${event.taskRun.taskType}\n" +
                    "Scheduled $scheduledFor, ${event.taskRun.status}: ${event.taskRun.resultMessage.orEmpty()}"
            }
        }
    }

    private fun sleepTriggerEngine(): SleepTriggerEngine {
        return SleepTriggerEngine(
            eventRepository = eventLogStore,
            taskScheduler = AlarmTaskScheduler(this)
        )
    }

    private fun buttonLayoutParams(): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}

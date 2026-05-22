package com.zzzzzz.sleeptrigger

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.zzzzzz.sleeptrigger.engine.SleepTriggerEngine
import com.zzzzzz.sleeptrigger.store.EventLogStore
import java.text.DateFormat
import java.util.Date

class MainActivity : Activity() {
    private lateinit var statusText: TextView
    private lateinit var eventLogStore: EventLogStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        eventLogStore = EventLogStore(this)

        statusText = TextView(this).apply {
            textSize = 16f
            setPadding(0, 0, 0, 24)
        }

        val testTriggerButton = Button(this).apply {
            text = "Sleep + 10 sec"
            setOnClickListener {
                SleepTriggerEngine(this@MainActivity).scheduleSimulatedSleepMediaPause(10_000)
                renderStatus()
            }
        }
        val fiveMinuteTriggerButton = Button(this).apply {
            text = "Sleep + 5 min"
            setOnClickListener {
                SleepTriggerEngine(this@MainActivity).scheduleSimulatedSleepMediaPause(5 * 60 * 1000)
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
                addView(statusText)
                addView(testTriggerButton, buttonLayoutParams())
                addView(fiveMinuteTriggerButton, buttonLayoutParams())
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

    private fun buttonLayoutParams(): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}

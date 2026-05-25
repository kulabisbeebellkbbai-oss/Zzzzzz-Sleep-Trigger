package com.zzzzzz.sleeptrigger.wear

import android.app.Activity
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.zzzzzz.sleeptrigger.shared.WearTriggerPayload
import java.text.DateFormat
import java.util.Date

class MainActivity : Activity() {
    private lateinit var statusText: TextView
    private lateinit var lastEventText: TextView
    private lateinit var transport: PhoneTriggerTransport
    private lateinit var passiveRegistrar: WearPassiveMonitoringRegistrar
    private var lastEvent: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transport = PhoneTriggerTransport(this)
        passiveRegistrar = WearPassiveMonitoringRegistrar(this)
        setContentView(buildContent())
        ensureActivityPermissionAndRegister()
        render()
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun buildContent(): View {
        return ScrollView(this).apply {
            setBackgroundColor(COLOR_BACKGROUND)
            addView(
                LinearLayout(this@MainActivity).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.CENTER_HORIZONTAL
                    setPadding(18, 14, 18, 18)

                    addView(
                        TextView(this@MainActivity).apply {
                            text = "Zzzzzz"
                            textSize = 20f
                            setTypeface(Typeface.DEFAULT, Typeface.BOLD)
                            setTextColor(COLOR_TEXT)
                            gravity = Gravity.CENTER
                        },
                        matchWrap()
                    )
                    addView(
                        TextView(this@MainActivity).apply {
                            text = "Watch triggers"
                            textSize = 12f
                            setTextColor(COLOR_MUTED)
                            gravity = Gravity.CENTER
                            setPadding(0, 0, 0, 8)
                        },
                        matchWrap()
                    )

                    statusText = statusPanel()
                    addView(statusText, matchWrap())

                    addView(
                        actionButton("Asleep") {
                            sendEvent("Asleep detected", "ASLEEP_DETECTED")
                        },
                        matchWrap(top = 8)
                    )
                    addView(
                        actionButton("Awake") {
                            sendEvent("Wake detected", "WAKE_DETECTED")
                        },
                        matchWrap(top = 6)
                    )
                    addView(
                        actionButton("Stood up") {
                            sendEvent("Stood up after wake", "STOOD_UP_AFTER_WAKE")
                        },
                        matchWrap(top = 6)
                    )

                    lastEventText = TextView(this@MainActivity).apply {
                        textSize = 11f
                        setTextColor(COLOR_TEXT)
                        gravity = Gravity.CENTER
                        setPadding(10, 8, 10, 0)
                    }
                    addView(lastEventText, matchWrap())
                }
            )
        }
    }

    private fun statusPanel(): TextView {
        return TextView(this).apply {
            textSize = 11f
            setTextColor(COLOR_TEXT)
            gravity = Gravity.CENTER
            setPadding(12, 8, 12, 8)
            background = rounded(COLOR_PANEL, COLOR_BORDER, 18f)
        }
    }

    private fun actionButton(textValue: String, onClick: () -> Unit): TextView {
        return TextView(this).apply {
            text = textValue
            textSize = 13f
            gravity = Gravity.CENTER
            setTextColor(0xFFFFFFFF.toInt())
            setPadding(8, 0, 8, 0)
            background = rounded(COLOR_ACTION, COLOR_ACTION, 24f)
            setOnClickListener { onClick() }
            isClickable = true
            isFocusable = true
            minHeight = 42
            minimumHeight = 42
        }
    }

    private fun sendEvent(name: String, triggerType: String) {
        val now = System.currentTimeMillis()
        val payload = WearTriggerPayload(
            eventId = "wear-$now",
            triggerType = triggerType,
            source = "WEAR_HEALTH_SERVICES",
            detectedAtMillis = now,
            confidence = 1.0f,
            metadata = mapOf("surface" to "manual-watch-ui")
        )
        val time = DateFormat.getTimeInstance(DateFormat.SHORT).format(Date())
        lastEvent = "$name queued\n$time"
        render()
        transport.send(payload) { result ->
            runOnUiThread {
                lastEvent = when (result) {
                    is PhoneTriggerTransport.TransportResult.Sent -> {
                        "$name sent to ${result.deliveredCount}/${result.targetCount} phone node(s)\n$time"
                    }
                    PhoneTriggerTransport.TransportResult.NoConnectedPhone -> {
                        "$name not sent: no connected phone node\n$time"
                    }
                    PhoneTriggerTransport.TransportResult.TransportUnavailable -> {
                        "$name not sent: Wear transport unavailable\n$time"
                    }
                }
                render()
            }
        }
    }

    private fun handleIntent(intent: Intent?) {
        when (intent?.action) {
            ACTION_SEND_TEST_TRIGGER -> {
                val triggerType = intent.getStringExtra(EXTRA_TRIGGER_TYPE) ?: "STOOD_UP_AFTER_WAKE"
                val name = when (triggerType) {
                    "ASLEEP_DETECTED" -> "Asleep detected"
                    "WAKE_DETECTED" -> "Wake detected"
                    "STOOD_UP_AFTER_WAKE" -> "Stood up after wake"
                    else -> "Wear trigger"
                }
                sendEvent(name, triggerType)
            }
            ACTION_REGISTER_PASSIVE_MONITORING -> {
                ensureActivityPermissionAndRegister()
            }
        }
    }

    private fun render() {
        statusText.text = "Transport ready.\n${passiveRegistrar.readStatus()}"
        lastEventText.text = lastEvent ?: getString(R.string.wear_status)
    }

    private fun ensureActivityPermissionAndRegister() {
        val missingPermissions = REQUIRED_PASSIVE_PERMISSIONS
            .filter { checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED }
            .toTypedArray()

        if (missingPermissions.isEmpty()) {
            passiveRegistrar.register {
                runOnUiThread { render() }
            }
        } else {
            requestPermissions(missingPermissions, REQUEST_PASSIVE_MONITORING_PERMISSIONS)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (
            requestCode == REQUEST_PASSIVE_MONITORING_PERMISSIONS &&
            grantResults.isNotEmpty() &&
            grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        ) {
            passiveRegistrar.register()
        }
        render()
    }

    private fun rounded(fill: Int, stroke: Int, radius: Float): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius
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
        const val ACTION_SEND_TEST_TRIGGER = "com.zzzzzz.sleeptrigger.wear.SEND_TEST_TRIGGER"
        const val ACTION_REGISTER_PASSIVE_MONITORING = "com.zzzzzz.sleeptrigger.wear.REGISTER_PASSIVE_MONITORING"
        const val EXTRA_TRIGGER_TYPE = "triggerType"
        const val REQUEST_PASSIVE_MONITORING_PERMISSIONS = 1001
        val REQUIRED_PASSIVE_PERMISSIONS = arrayOf(
            Manifest.permission.ACTIVITY_RECOGNITION,
            Manifest.permission.BODY_SENSORS
        )
        const val COLOR_BACKGROUND = 0xFF101416.toInt()
        const val COLOR_PANEL = 0xFF1F292C.toInt()
        const val COLOR_TEXT = 0xFFF3F7F6.toInt()
        const val COLOR_MUTED = 0xFFA8B5B3.toInt()
        const val COLOR_BORDER = 0xFF344244.toInt()
        const val COLOR_ACTION = 0xFF2B8A75.toInt()
    }
}

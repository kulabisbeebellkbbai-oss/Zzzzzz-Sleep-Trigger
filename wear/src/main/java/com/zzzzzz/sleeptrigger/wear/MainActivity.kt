package com.zzzzzz.sleeptrigger.wear

import android.app.Activity
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.text.DateFormat
import java.util.Date

class MainActivity : Activity() {
    private lateinit var statusText: TextView
    private lateinit var lastEventText: TextView
    private var lastEvent: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(buildContent())
        render()
    }

    private fun buildContent(): View {
        return ScrollView(this).apply {
            setBackgroundColor(COLOR_BACKGROUND)
            addView(
                LinearLayout(this@MainActivity).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.CENTER_HORIZONTAL
                    setPadding(18, 20, 18, 28)

                    addView(
                        TextView(this@MainActivity).apply {
                            text = "Zzzzzz"
                            textSize = 24f
                            setTypeface(Typeface.DEFAULT, Typeface.BOLD)
                            setTextColor(COLOR_TEXT)
                            gravity = Gravity.CENTER
                        },
                        matchWrap()
                    )
                    addView(
                        TextView(this@MainActivity).apply {
                            text = "Watch triggers"
                            textSize = 13f
                            setTextColor(COLOR_MUTED)
                            gravity = Gravity.CENTER
                            setPadding(0, 2, 0, 14)
                        },
                        matchWrap()
                    )

                    statusText = statusPanel()
                    addView(statusText, matchWrap())

                    addView(
                        actionButton("Asleep") {
                            recordEvent("Asleep detected")
                        },
                        matchWrap(top = 12)
                    )
                    addView(
                        actionButton("Awake") {
                            recordEvent("Wake detected")
                        },
                        matchWrap(top = 8)
                    )
                    addView(
                        actionButton("Stood up") {
                            recordEvent("Stood up after wake")
                        },
                        matchWrap(top = 8)
                    )

                    lastEventText = TextView(this@MainActivity).apply {
                        textSize = 12f
                        setTextColor(COLOR_TEXT)
                        gravity = Gravity.CENTER
                        setPadding(10, 14, 10, 0)
                    }
                    addView(lastEventText, matchWrap())
                }
            )
        }
    }

    private fun statusPanel(): TextView {
        return TextView(this).apply {
            textSize = 13f
            setTextColor(COLOR_TEXT)
            gravity = Gravity.CENTER
            setPadding(14, 14, 14, 14)
            background = rounded(COLOR_PANEL, COLOR_BORDER, 18f)
        }
    }

    private fun actionButton(textValue: String, onClick: () -> Unit): Button {
        return Button(this).apply {
            text = textValue
            textSize = 13f
            isAllCaps = false
            setTextColor(0xFFFFFFFF.toInt())
            background = rounded(COLOR_ACTION, COLOR_ACTION, 24f)
            setOnClickListener { onClick() }
            minHeight = 46
        }
    }

    private fun recordEvent(name: String) {
        val time = DateFormat.getTimeInstance(DateFormat.SHORT).format(Date())
        lastEvent = "$name\n$time"
        render()
    }

    private fun render() {
        statusText.text = "Ready to send sleep and wake events once phone transport is connected."
        lastEventText.text = lastEvent ?: getString(R.string.wear_status)
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
        const val COLOR_BACKGROUND = 0xFF101416.toInt()
        const val COLOR_PANEL = 0xFF1F292C.toInt()
        const val COLOR_TEXT = 0xFFF3F7F6.toInt()
        const val COLOR_MUTED = 0xFFA8B5B3.toInt()
        const val COLOR_BORDER = 0xFF344244.toInt()
        const val COLOR_ACTION = 0xFF2B8A75.toInt()
    }
}

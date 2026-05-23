package com.zzzzzz.sleeptrigger.testmedia

import android.app.Activity
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    private lateinit var statusText: TextView
    private lateinit var mediaSession: MediaSession

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        statusText = TextView(this).apply {
            textSize = 18f
            setPadding(0, 0, 0, 24)
        }
        mediaSession = MediaSession(this, "ZzzzzzTestMedia").apply {
            setCallback(
                object : MediaSession.Callback() {
                    override fun onPause() {
                        recordPaused()
                    }

                    override fun onStop() {
                        recordPaused()
                    }
                }
            )
            isActive = true
        }

        setContentView(
            LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(32, 32, 32, 32)
                addView(statusText)
                addView(
                    Button(this@MainActivity).apply {
                        text = "Start playing"
                        isAllCaps = false
                        setOnClickListener { startPlaying() }
                    },
                    matchWrap()
                )
                addView(
                    Button(this@MainActivity).apply {
                        text = "Reset"
                        isAllCaps = false
                        setOnClickListener { reset() }
                    },
                    matchWrap()
                )
            }
        )
        startPlaying()
    }

    override fun onResume() {
        super.onResume()
        render()
    }

    override fun onDestroy() {
        mediaSession.release()
        super.onDestroy()
    }

    private fun startPlaying() {
        preferences().edit()
            .putBoolean(KEY_PLAYING, true)
            .putBoolean(KEY_PAUSED_BY_CONTROLLER, false)
            .apply()
        mediaSession.setPlaybackState(
            PlaybackState.Builder()
                .setActions(PlaybackState.ACTION_PLAY or PlaybackState.ACTION_PAUSE or PlaybackState.ACTION_STOP)
                .setState(PlaybackState.STATE_PLAYING, 0, 1.0f)
                .build()
        )
        render()
    }

    private fun recordPaused() {
        preferences().edit()
            .putBoolean(KEY_PLAYING, false)
            .putBoolean(KEY_PAUSED_BY_CONTROLLER, true)
            .putLong(KEY_LAST_PAUSE_MILLIS, System.currentTimeMillis())
            .apply()
        mediaSession.setPlaybackState(
            PlaybackState.Builder()
                .setActions(PlaybackState.ACTION_PLAY or PlaybackState.ACTION_PAUSE or PlaybackState.ACTION_STOP)
                .setState(PlaybackState.STATE_PAUSED, 0, 0.0f)
                .build()
        )
        runOnUiThread { render() }
    }

    private fun reset() {
        preferences().edit().clear().apply()
        startPlaying()
    }

    private fun render() {
        val preferences = preferences()
        statusText.text = "Playing: ${preferences.getBoolean(KEY_PLAYING, false)}\n" +
            "Paused by controller: ${preferences.getBoolean(KEY_PAUSED_BY_CONTROLLER, false)}\n" +
            "Last pause: ${preferences.getLong(KEY_LAST_PAUSE_MILLIS, 0)}"
    }

    private fun preferences() = getSharedPreferences("test-media-state", MODE_PRIVATE)

    private fun matchWrap(): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private companion object {
        const val KEY_PLAYING = "playing"
        const val KEY_PAUSED_BY_CONTROLLER = "pausedByController"
        const val KEY_LAST_PAUSE_MILLIS = "lastPauseMillis"
    }
}


package com.zzzzzz.sleeptrigger.media

import android.content.ComponentName
import android.content.Context
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState

class MediaPauseTask(private val context: Context) {
    fun pauseActiveSessions(): MediaPauseResult {
        val sessionManager = context.getSystemService(MediaSessionManager::class.java)
        val listenerComponent = ComponentName(context, MediaPauseNotificationListener::class.java)
        val activeSessions = try {
            sessionManager.getActiveSessions(listenerComponent)
        } catch (exception: SecurityException) {
            return MediaPauseResult(
                pausedPackageNames = emptyList(),
                failedPackageNames = emptyList(),
                message = "Notification listener access is not enabled.",
                succeeded = false
            )
        }

        val activeMediaSessions = activeSessions.filter { it.playbackState.isPlaying() }
        val paused = mutableListOf<String>()
        val failed = mutableListOf<String>()
        for (controller in activeMediaSessions) {
            try {
                controller.transportControls.pause()
                paused += controller.packageName
            } catch (exception: RuntimeException) {
                failed += controller.packageName
            }
        }

        val message = when {
            activeMediaSessions.isEmpty() -> "No active media sessions found."
            failed.isEmpty() -> "Paused ${paused.size} active media session(s)."
            else -> "Paused ${paused.size}; failed ${failed.size} media session(s)."
        }
        return MediaPauseResult(
            pausedPackageNames = paused.distinct(),
            failedPackageNames = failed.distinct(),
            message = message,
            succeeded = failed.isEmpty()
        )
    }

    private fun PlaybackState?.isPlaying(): Boolean {
        return this?.state in setOf(
            PlaybackState.STATE_PLAYING,
            PlaybackState.STATE_BUFFERING,
            PlaybackState.STATE_FAST_FORWARDING,
            PlaybackState.STATE_REWINDING,
            PlaybackState.STATE_SKIPPING_TO_NEXT,
            PlaybackState.STATE_SKIPPING_TO_PREVIOUS,
            PlaybackState.STATE_SKIPPING_TO_QUEUE_ITEM
        )
    }
}

data class MediaPauseResult(
    val pausedPackageNames: List<String>,
    val failedPackageNames: List<String>,
    val message: String,
    val succeeded: Boolean
)

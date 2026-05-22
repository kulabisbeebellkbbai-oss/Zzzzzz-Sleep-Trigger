package com.zzzzzz.sleeptrigger.permissions

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings

class AndroidPermissionStatusReader(private val context: Context) {
    fun read(): PermissionStatus {
        return PermissionStatus(
            notificationListenerEnabled = isNotificationListenerEnabled(),
            notificationsEnabled = notificationsEnabled(),
            activityRecognitionGranted = activityRecognitionGranted()
        )
    }

    private fun isNotificationListenerEnabled(): Boolean {
        val enabled = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        ).orEmpty()
        return enabled.contains(context.packageName)
    }

    private fun notificationsEnabled(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return context.getSystemService(NotificationManager::class.java).areNotificationsEnabled()
    }

    private fun activityRecognitionGranted(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return true
        return context.checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION) ==
            PackageManager.PERMISSION_GRANTED
    }
}

package com.zzzzzz.sleeptrigger.permissions

data class PermissionStatus(
    val notificationListenerEnabled: Boolean,
    val notificationsEnabled: Boolean,
    val activityRecognitionGranted: Boolean
)


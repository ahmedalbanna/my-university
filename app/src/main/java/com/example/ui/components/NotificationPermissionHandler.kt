package com.example.ui.components

import android.Manifest
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

/**
 * P1.4 — Runtime permission for POST_NOTIFICATIONS (Android 13+).
 * Requests once on first composition; required before RingtoneManager / notifications.
 * No-op on <33.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestNotificationPermissionIfNeeded() {
    if (Build.VERSION.SDK_INT < 33) return
    val state = rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    LaunchedEffect(Unit) {
        if (!state.status.isGranted) {
            state.launchPermissionRequest()
        }
    }
}

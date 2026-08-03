package com.reefii.aeromute

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import com.reefii.aeromute.data.AeroMutePreferences
import com.reefii.aeromute.data.DarkModeOption
import com.reefii.aeromute.service.FloatingMuteService
import com.reefii.aeromute.ui.screens.AeroMuteMainScreen
import com.reefii.aeromute.ui.theme.AeroMuteTheme

class MainActivity : ComponentActivity() {

    private lateinit var preferences: AeroMutePreferences

    private var isOverlayGranted by mutableStateOf(false)
    private var isDndGranted by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        preferences = AeroMutePreferences.getInstance(applicationContext)

        checkPermissions()

        setContent {
            val settings by preferences.settings.collectAsState()
            val isDarkTheme = when (settings.darkModeOption) {
                DarkModeOption.SYSTEM -> isSystemInDarkTheme()
                DarkModeOption.DARK -> true
                DarkModeOption.LIGHT -> false
            }

            AeroMuteTheme(darkTheme = isDarkTheme) {
                // Re-check permissions whenever user comes back from Settings screen
                LifecycleResumeEffect(Unit) {
                    checkPermissions()
                    onPauseOrDispose { }
                }

                AeroMuteMainScreen(
                    preferences = preferences,
                    isOverlayGranted = isOverlayGranted,
                    isDndGranted = isDndGranted,
                    onRequestOverlay = { requestOverlayPermission() },
                    onRequestDnd = { requestDndPermission() },
                    onToggleService = { enable ->
                        toggleFloatingService(enable)
                    }
                )
            }
        }
    }

    private fun checkPermissions() {
        isOverlayGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        isDndGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notificationManager.isNotificationPolicyAccessGranted
        } else {
            true
        }
    }

    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        }
    }

    private fun requestDndPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (!notificationManager.isNotificationPolicyAccessGranted) {
                val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                startActivity(intent)
            }
        }
    }

    private fun toggleFloatingService(enable: Boolean) {
        if (enable) {
            if (!isOverlayGranted) {
                requestOverlayPermission()
                return
            }

            val intent = Intent(this, FloatingMuteService::class.java).apply {
                action = FloatingMuteService.ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
            preferences.updateServiceState(true)
        } else {
            val intent = Intent(this, FloatingMuteService::class.java)
            stopService(intent)
            preferences.updateServiceState(false)
        }
    }
}


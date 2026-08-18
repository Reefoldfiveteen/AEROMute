package com.reefii.aeromute.service

import com.reefii.aeromute.R
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.VolumeDown
import androidx.compose.material.icons.rounded.VolumeMute
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.reefii.aeromute.MainActivity
import com.reefii.aeromute.data.AeroMutePreferences
import com.reefii.aeromute.data.AeroThemePreset
import com.reefii.aeromute.data.AudioStreamManager
import com.reefii.aeromute.data.AudioStreamType
import com.reefii.aeromute.data.StreamVolumeInfo
import com.reefii.aeromute.data.WidgetScale
import com.reefii.aeromute.ui.theme.AeroMuteTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.math.abs
import kotlin.math.roundToInt

class FloatingMuteService : Service(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    private lateinit var windowManager: WindowManager
    private var floatingView: ComposeView? = null
    private var layoutParams: WindowManager.LayoutParams? = null

    private lateinit var preferences: AeroMutePreferences
    private lateinit var audioStreamManager: AudioStreamManager

    // Service Lifecycle objects for Compose View
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val store = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = store
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    private var isExpanded = MutableStateFlow(false)
    private var isLockedState = MutableStateFlow(false)

    // Auto-collapse handler
    private val handler = Handler(Looper.getMainLooper())
    private val autoCollapseRunnable = Runnable {
        isExpanded.value = false
    }

    private val volumeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                if (preferences.settings.value.autoMuteOnHeadsetDisconnect) {
                    audioStreamManager.setStreamVolume(com.reefii.aeromute.data.AudioStreamType.MEDIA, 0)
                }
            }
            // Trigger recomposition by refreshing stream flow
            volumeTriggerState.value += 1
        }
    }

    private val volumeTriggerState = MutableStateFlow(0)
    private val backupVolumes = mutableMapOf<AudioStreamType, Int>()

    override fun onCreate() {
        super.onCreate()
        try {
            savedStateRegistryController.performAttach()
            savedStateRegistryController.performRestore(null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        preferences = AeroMutePreferences.getInstance(applicationContext)
        audioStreamManager = AudioStreamManager(applicationContext)

        // Register Volume Change Broadcast Receiver with exported flag for Android 13/14+
        val filter = IntentFilter("android.media.VOLUME_CHANGED_ACTION").apply {
            addAction(VolumeChangeReceiver.ACTION_VOLUME_UPDATED)
            addAction(android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY)
            addAction(Intent.ACTION_HEADSET_PLUG)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(volumeReceiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            registerReceiver(volumeReceiver, filter)
        }

        startForegroundNotification()
        setupFloatingWindow()

        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                preferences.updateServiceState(false)
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_TOGGLE_MUTE -> {
                val nowMuted = audioStreamManager.muteAllActiveStreams(preferences.settings.value, backupVolumes)
                preferences.updateMuteAll(nowMuted)
                vibrateFeedback()
                volumeTriggerState.value += 1
                updateNotification(preferences.settings.value.isOverlayHidden, nowMuted)
            }
            ACTION_TOGGLE_VISIBILITY -> {
                val currentHidden = preferences.settings.value.isOverlayHidden
                val newHidden = !currentHidden
                preferences.updateOverlayHidden(newHidden)
                floatingView?.visibility = if (newHidden) View.GONE else View.VISIBLE
                updateNotification(newHidden, preferences.settings.value.isMutedAll)
                vibrateFeedback()
            }
            ACTION_SHOW_OVERLAY -> {
                preferences.updateOverlayHidden(false)
                floatingView?.visibility = View.VISIBLE
                updateNotification(false, preferences.settings.value.isMutedAll)
            }
            ACTION_HIDE_OVERLAY -> {
                preferences.updateOverlayHidden(true)
                floatingView?.visibility = View.GONE
                updateNotification(true, preferences.settings.value.isMutedAll)
            }
            ACTION_START -> {
                preferences.updateServiceState(true)
                floatingView?.visibility = if (preferences.settings.value.isOverlayHidden) View.GONE else View.VISIBLE
                updateNotification(preferences.settings.value.isOverlayHidden, preferences.settings.value.isMutedAll)
            }
        }
        return START_STICKY
    }

    private fun setupFloatingWindow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            // Cannot show overlay without permission
            preferences.updateServiceState(false)
            return
        }

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val settings = preferences.settings.value
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels

        val initialXPos = if (settings.savedX != -1) settings.savedX else screenWidth - 180
        val initialYPos = settings.savedY

        layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = initialXPos
            y = initialYPos
        }

        isLockedState.value = settings.isLocked

        floatingView = ComposeView(this).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setViewTreeLifecycleOwner(this@FloatingMuteService)
            setViewTreeViewModelStoreOwner(this@FloatingMuteService)
            setViewTreeSavedStateRegistryOwner(this@FloatingMuteService)

            setContent {
                AeroMuteTheme {
                    val currentSettings by preferences.settings.collectAsState()
                    val expanded by isExpanded.collectAsState()
                    val locked by isLockedState.collectAsState()
                    val triggerCount by volumeTriggerState.collectAsState()

                    // Re-calculate stream volumes when triggerCount changes
                    val streams = remember(triggerCount, currentSettings) {
                        audioStreamManager.getAllStreams(currentSettings)
                    }

                    val mediaStream = streams.firstOrNull { it.type == AudioStreamType.MEDIA }
                    val isMediaZeroOrMuted = (mediaStream?.isMuted == true || (mediaStream?.currentVolume ?: 0) == 0)
                    val isBadgeMuted = currentSettings.isMutedAll || isMediaZeroOrMuted

                    LaunchedEffect(currentSettings.isOverlayHidden) {
                        floatingView?.visibility = if (currentSettings.isOverlayHidden) View.GONE else View.VISIBLE
                        updateNotification(currentSettings.isOverlayHidden, currentSettings.isMutedAll)
                    }

                    Box(
                        modifier = Modifier
                            .pointerInput(expanded) {
                                if (!expanded) {
                                    detectTapGestures(
                                        onTap = {
                                            resetAutoCollapseTimer(currentSettings.autoCollapseSeconds)
                                            isExpanded.value = !isExpanded.value
                                            vibrateFeedback()
                                        },
                                        onDoubleTap = {
                                            resetAutoCollapseTimer(currentSettings.autoCollapseSeconds)
                                            val nowMuted = audioStreamManager.muteAllActiveStreams(currentSettings, backupVolumes)
                                            preferences.updateMuteAll(nowMuted)
                                            volumeTriggerState.value += 1
                                            vibrateFeedback()
                                            updateNotification(currentSettings.isOverlayHidden, nowMuted)
                                        }
                                    )
                                }
                            }
                            .floatingWindowDrag(
                                isLocked = locked,
                                settings = currentSettings
                            )
                    ) {
                        if (!expanded) {
                            // Collapsed Floating Badge / Bubble
                            CollapsedFloatingBadge(
                                settings = currentSettings,
                                isMuted = isBadgeMuted,
                                streams = streams
                            )
                        } else {
                            if (currentSettings.floatingMode == com.reefii.aeromute.data.FloatingWidgetMode.SIMPLE) {
                                SimpleFloatingPanel(
                                    settings = currentSettings,
                                    streams = streams,
                                    isLocked = locked,
                                    onLockToggle = {
                                        val newLock = !locked
                                        isLockedState.value = newLock
                                        preferences.updateLocked(newLock)
                                        vibrateFeedback()
                                    },
                                    onHide = {
                                        isExpanded.value = false
                                        preferences.updateOverlayHidden(true)
                                        floatingView?.visibility = View.GONE
                                        updateNotification(true, currentSettings.isMutedAll)
                                        vibrateFeedback()
                                    },
                                    onClose = { isExpanded.value = false },
                                    onVolumeChange = { streamType, newVol ->
                                        resetAutoCollapseTimer(currentSettings.autoCollapseSeconds)
                                        audioStreamManager.setStreamVolume(streamType, newVol)
                                        volumeTriggerState.value += 1
                                    },
                                    onMuteAllToggle = {
                                        resetAutoCollapseTimer(currentSettings.autoCollapseSeconds)
                                        val muted = audioStreamManager.muteAllActiveStreams(currentSettings, backupVolumes, excludeAlarm = true)
                                        preferences.updateMuteAll(muted)
                                        volumeTriggerState.value += 1
                                        vibrateFeedback()
                                        updateNotification(currentSettings.isOverlayHidden, muted)
                                    },
                                    onResetFiftyPercent = {
                                        resetAutoCollapseTimer(currentSettings.autoCollapseSeconds)
                                        for (st in streams) {
                                            if (st.type != AudioStreamType.ALARM) {
                                                val vol50 = (st.maxVolume * 0.5f).toInt().coerceAtLeast(1)
                                                audioStreamManager.setStreamVolume(st.type, vol50)
                                            }
                                        }
                                        preferences.updateMuteAll(false)
                                        volumeTriggerState.value += 1
                                        vibrateFeedback()
                                        updateNotification(currentSettings.isOverlayHidden, false)
                                    }
                                )
                            } else {
                                // Expanded Control Panel
                                ExpandedFloatingPanel(
                                    settings = currentSettings,
                                    streams = streams,
                                    isLocked = locked,
                                    onLockToggle = {
                                        val newLock = !locked
                                        isLockedState.value = newLock
                                        preferences.updateLocked(newLock)
                                        vibrateFeedback()
                                    },
                                    onHide = {
                                        isExpanded.value = false
                                        preferences.updateOverlayHidden(true)
                                        floatingView?.visibility = View.GONE
                                        updateNotification(true, currentSettings.isMutedAll)
                                        vibrateFeedback()
                                    },
                                    onClose = {
                                        isExpanded.value = false
                                    },
                                    onVolumeChange = { streamType, newVol ->
                                        resetAutoCollapseTimer(currentSettings.autoCollapseSeconds)
                                        audioStreamManager.setStreamVolume(streamType, newVol)
                                        volumeTriggerState.value += 1
                                    },
                                    onMuteToggle = { streamType ->
                                        resetAutoCollapseTimer(currentSettings.autoCollapseSeconds)
                                        audioStreamManager.toggleStreamMute(streamType)
                                        volumeTriggerState.value += 1
                                        vibrateFeedback()
                                    },
                                    onMuteAllToggle = {
                                        resetAutoCollapseTimer(currentSettings.autoCollapseSeconds)
                                        val muted = audioStreamManager.muteAllActiveStreams(currentSettings, backupVolumes)
                                        preferences.updateMuteAll(muted)
                                        volumeTriggerState.value += 1
                                        vibrateFeedback()
                                    },
                                    onResetFiftyPercent = {
                                        resetAutoCollapseTimer(currentSettings.autoCollapseSeconds)
                                        for (st in streams) {
                                            val vol50 = (st.maxVolume * 0.5f).toInt().coerceAtLeast(1)
                                            audioStreamManager.setStreamVolume(st.type, vol50)
                                        }
                                        preferences.updateMuteAll(false)
                                        volumeTriggerState.value += 1
                                        vibrateFeedback()
                                    },
                                    onOpenSettings = {
                                        isExpanded.value = false
                                        val intent = Intent(applicationContext, MainActivity::class.java).apply {
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        startActivity(intent)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        try {
            windowManager.addView(floatingView, layoutParams)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun Modifier.floatingWindowDrag(
        isLocked: Boolean,
        settings: com.reefii.aeromute.data.AeroMuteSettings
    ): Modifier = this.pointerInput(isLocked, settings.snapToEdge, settings.widgetScale) {
        if (isLocked) return@pointerInput

        detectDragGestures(
            onDragStart = {
                resetAutoCollapseTimer(settings.autoCollapseSeconds)
            },
            onDrag = { change, dragAmount ->
                change.consume()
                val currentX = layoutParams?.x ?: 0
                val currentY = layoutParams?.y ?: 0
                layoutParams?.x = currentX + dragAmount.x.toInt()
                layoutParams?.y = currentY + dragAmount.y.toInt()
                try {
                    windowManager.updateViewLayout(floatingView, layoutParams)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            onDragEnd = {
                val screenWidthPx = resources.displayMetrics.widthPixels
                val density = resources.displayMetrics.density
                var finalX = layoutParams?.x ?: 0
                val finalY = layoutParams?.y ?: 0

                if (settings.snapToEdge) {
                    val sizePx = (settings.widgetScale.sizeDp * density).toInt()
                    finalX = if (finalX + sizePx / 2 < screenWidthPx / 2) {
                        16
                    } else {
                        (screenWidthPx - sizePx - 24).coerceAtLeast(16)
                    }
                    layoutParams?.x = finalX
                    try {
                        windowManager.updateViewLayout(floatingView, layoutParams)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                preferences.savePosition(finalX, finalY)
            }
        )
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun resetAutoCollapseTimer(seconds: Int) {
        handler.removeCallbacks(autoCollapseRunnable)
        if (seconds > 0) {
            handler.postDelayed(autoCollapseRunnable, seconds * 1000L)
        }
    }

    private fun vibrateFeedback() {
        if (preferences.settings.value.vibrateOnMute) {
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(35)
                }
            }
        }
    }

    private fun startForegroundNotification() {
        val channelId = "aeromute_floating_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "AEROMute Floating Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifikasi status kontrol volume melayang AEROMute"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notif = buildForegroundNotification(
            channelId = channelId,
            isOverlayHidden = preferences.settings.value.isOverlayHidden,
            isMuted = preferences.settings.value.isMutedAll
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val type = android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            startForeground(NOTIFICATION_ID, notif, type)
        } else {
            startForeground(NOTIFICATION_ID, notif)
        }
    }

    private fun updateNotification(isOverlayHidden: Boolean, isMuted: Boolean) {
        val channelId = "aeromute_floating_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notif = buildForegroundNotification(channelId, isOverlayHidden, isMuted)
        notificationManager.notify(NOTIFICATION_ID, notif)
    }

    private fun buildForegroundNotification(
        channelId: String,
        isOverlayHidden: Boolean,
        isMuted: Boolean
    ): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopIntent = Intent(this, FloatingMuteService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 1, stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val muteToggleIntent = Intent(this, FloatingMuteService::class.java).apply {
            action = ACTION_TOGGLE_MUTE
        }
        val mutePendingIntent = PendingIntent.getService(
            this, 2, muteToggleIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val visibilityIntent = Intent(this, FloatingMuteService::class.java).apply {
            action = ACTION_TOGGLE_VISIBILITY
        }
        val visibilityPendingIntent = PendingIntent.getService(
            this, 3, visibilityIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val isIndo = preferences.settings.value.appLanguage == com.reefii.aeromute.data.AppLanguage.INDONESIAN
        val muteActionLabel = if (isMuted) {
            if (isIndo) "Suarakan (Unmute)" else "Unmute"
        } else {
            if (isIndo) "Senyapkan (Mute)" else "Quick Mute"
        }

        val visibilityActionLabel = if (isOverlayHidden) {
            if (isIndo) "Tampilkan Widget" else "Show Widget"
        } else {
            if (isIndo) "Sembunyikan" else "Hide Widget"
        }

        val stopLabel = if (isIndo) "Matikan" else "Turn Off"

        val contentStatus = if (isOverlayHidden) {
            if (isIndo) "Widget melayang tersembunyi (Mode Hemat Daya)" else "Floating widget hidden (Power-saving mode)"
        } else {
            if (isIndo) "Widget melayang aktif di layar" else "Floating widget is active on screen"
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("AEROMute PRO")
            .setContentText(contentStatus)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .addAction(R.drawable.ic_launcher_foreground, muteActionLabel, mutePendingIntent)
            .addAction(R.drawable.ic_launcher_foreground, visibilityActionLabel, visibilityPendingIntent)
            .addAction(R.drawable.ic_launcher_foreground, stopLabel, stopPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onDestroy() {
        handler.removeCallbacks(autoCollapseRunnable)
        try {
            unregisterReceiver(volumeReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (floatingView != null) {
            try {
                windowManager.removeView(floatingView)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            floatingView = null
        }

        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        super.onDestroy()
    }

    // Compose Collapsed Floating Badge Component
    @Composable
    private fun CollapsedFloatingBadge(
        settings: com.reefii.aeromute.data.AeroMuteSettings,
        isMuted: Boolean,
        streams: List<StreamVolumeInfo>
    ) {
        val sizeDp = settings.widgetScale.sizeDp.dp
        val alpha = settings.transparency

        val themePrimary = Color(settings.themePreset.primaryColorHex)
        val containerBg = if (isMuted) Color(0xFFF43F5E).copy(alpha = alpha)
        else themePrimary.copy(alpha = alpha)

        val activeMediaStream = streams.firstOrNull { it.type == AudioStreamType.MEDIA }
        val volPct = activeMediaStream?.percentage ?: 0

        val badgeShape: androidx.compose.ui.graphics.Shape = when (settings.widgetShape) {
            com.reefii.aeromute.data.WidgetShape.CIRCLE -> CircleShape
            com.reefii.aeromute.data.WidgetShape.SQUIRCLE -> RoundedCornerShape(16.dp)
            com.reefii.aeromute.data.WidgetShape.PILL -> RoundedCornerShape(24.dp)
            com.reefii.aeromute.data.WidgetShape.TEARDROP -> RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp, bottomStart = 4.dp, bottomEnd = 28.dp)
        }

        Surface(
            modifier = Modifier
                .size(sizeDp)
                .shadow(12.dp, badgeShape)
                .clip(badgeShape)
                .border(2.dp, Color.White.copy(alpha = alpha * 0.6f), badgeShape),
            color = containerBg,
            contentColor = Color.White
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(4.dp)
            ) {
                if (isMuted) {
                    Icon(
                        imageVector = Icons.Rounded.VolumeOff,
                        contentDescription = "Muted",
                        modifier = Modifier.size((sizeDp.value * 0.5f).dp),
                        tint = Color.White
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = when {
                                volPct == 0 -> Icons.Rounded.VolumeMute
                                volPct < 50 -> Icons.Rounded.VolumeDown
                                else -> Icons.Rounded.VolumeUp
                            },
                            contentDescription = "Volume Active",
                            modifier = Modifier.size((sizeDp.value * 0.44f).dp),
                            tint = Color.White
                        )
                        Text(
                            text = "$volPct%",
                            fontSize = (sizeDp.value * 0.22f).sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }

    // Compose Expanded Control Panel Component
    @Composable
    private fun ExpandedFloatingPanel(
        settings: com.reefii.aeromute.data.AeroMuteSettings,
        streams: List<StreamVolumeInfo>,
        isLocked: Boolean,
        onLockToggle: () -> Unit,
        onHide: () -> Unit,
        onClose: () -> Unit,
        onVolumeChange: (AudioStreamType, Int) -> Unit,
        onMuteToggle: (AudioStreamType) -> Unit,
        onMuteAllToggle: () -> Unit,
        onResetFiftyPercent: () -> Unit,
        onOpenSettings: () -> Unit
    ) {
        val alpha = settings.transparency
        val themePrimary = Color(settings.themePreset.primaryColorHex)

        val isAllMuted = streams.isNotEmpty() && streams.all { it.isMuted || it.currentVolume == 0 }

        Card(
            modifier = Modifier
                .width(310.dp)
                .padding(6.dp)
                .shadow(16.dp, RoundedCornerShape(24.dp))
                .border(1.5.dp, Color.White.copy(alpha = alpha * 0.5f), RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF0F172A).copy(alpha = alpha.coerceAtLeast(0.78f))
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(32.dp),
                            shape = CircleShape,
                            color = themePrimary
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isAllMuted) Icons.Rounded.VolumeOff else Icons.Rounded.VolumeUp,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AEROMute",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            color = Color.White
                        )
                    }

                    Row {
                        IconButton(
                            onClick = onLockToggle,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (isLocked) Icons.Rounded.Lock else Icons.Rounded.LockOpen,
                                contentDescription = "Lock Drag",
                                tint = if (isLocked) Color(0xFFF59E0B) else Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = onHide,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.VisibilityOff,
                                contentDescription = "Hide Widget",
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = onOpenSettings,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Settings,
                                contentDescription = "Open Settings",
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = onClose,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Close",
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quick Action Bar (Mute All & Reset Buttons)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onMuteAllToggle,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isAllMuted) Color(0xFF10B981) else Color(0xFFF43F5E)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isAllMuted) Icons.Rounded.VolumeUp else Icons.Rounded.VolumeOff,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isAllMuted) "UNMUTE ALL" else "MUTE ALL",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Button(
                        onClick = onResetFiftyPercent,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF334155)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "RESET 50%",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Stream Sliders
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    for (stream in streams) {
                        StreamSliderItem(
                            stream = stream,
                            themePrimary = themePrimary,
                            onVolumeChange = { newVol -> onVolumeChange(stream.type, newVol) },
                            onMuteToggle = { onMuteToggle(stream.type) }
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun StreamSliderItem(
        stream: StreamVolumeInfo,
        themePrimary: Color,
        onVolumeChange: (Int) -> Unit,
        onMuteToggle: () -> Unit
    ) {
        val icon = when (stream.type) {
            AudioStreamType.MEDIA -> Icons.Rounded.MusicNote
            AudioStreamType.RING -> Icons.Rounded.Notifications
            AudioStreamType.NOTIFICATION -> Icons.Rounded.NotificationsActive
            AudioStreamType.ALARM -> Icons.Rounded.Alarm
            AudioStreamType.CALL -> Icons.Rounded.Call
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon / Mute Button
            Surface(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .clickable { onMuteToggle() },
                color = if (stream.isMuted || stream.currentVolume == 0) Color(0xFFF43F5E).copy(alpha = 0.25f)
                else Color(0xFF334155)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (stream.isMuted || stream.currentVolume == 0) Icons.Rounded.VolumeOff else icon,
                        contentDescription = stream.type.title,
                        tint = if (stream.isMuted || stream.currentVolume == 0) Color(0xFFF43F5E) else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Title & Slider
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stream.type.title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Text(
                        text = "${stream.currentVolume}/${stream.maxVolume}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (stream.isMuted || stream.currentVolume == 0) Color(0xFFF43F5E)
                        else themePrimary
                    )
                }

                Slider(
                    value = stream.currentVolume.toFloat(),
                    onValueChange = { onVolumeChange(it.toInt()) },
                    valueRange = 0f..stream.maxVolume.toFloat(),
                    steps = (stream.maxVolume - 1).coerceAtLeast(0),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(26.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = if (stream.isMuted || stream.currentVolume == 0) Color(0xFFF43F5E) else themePrimary,
                        activeTrackColor = if (stream.isMuted || stream.currentVolume == 0) Color(0xFFF43F5E).copy(alpha = 0.6f) else themePrimary,
                        inactiveTrackColor = Color(0xFF334155)
                    )
                )
            }
        }
    }

    @Composable
    private fun SimpleFloatingPanel(
        settings: com.reefii.aeromute.data.AeroMuteSettings,
        streams: List<StreamVolumeInfo>,
        isLocked: Boolean,
        onLockToggle: () -> Unit,
        onHide: () -> Unit,
        onClose: () -> Unit,
        onVolumeChange: (AudioStreamType, Int) -> Unit,
        onMuteAllToggle: () -> Unit,
        onResetFiftyPercent: () -> Unit
    ) {
        val alpha = settings.transparency
        val themePrimary = Color(settings.themePreset.primaryColorHex)

        // Exclude ALARM from simple mode calculations
        val nonAlarmStreams = streams.filter { it.type != AudioStreamType.ALARM }
        val isAllMuted = nonAlarmStreams.isNotEmpty() && nonAlarmStreams.all { it.isMuted || it.currentVolume == 0 }

        val primaryStream = nonAlarmStreams.firstOrNull { it.type == AudioStreamType.MEDIA }
            ?: nonAlarmStreams.firstOrNull()
            ?: StreamVolumeInfo(AudioStreamType.MEDIA, 0, 15, false)

        val pct = primaryStream.percentage

        Card(
            modifier = Modifier
                .width(155.dp)
                .padding(6.dp)
                .shadow(16.dp, RoundedCornerShape(24.dp))
                .border(1.5.dp, Color.White.copy(alpha = alpha * 0.5f), RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF0F172A).copy(alpha = alpha.coerceAtLeast(0.85f))
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header (Lock, Hide & Close)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onLockToggle,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (isLocked) Icons.Rounded.Lock else Icons.Rounded.LockOpen,
                            contentDescription = "Lock",
                            tint = if (isLocked) Color(0xFFF59E0B) else Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    IconButton(
                        onClick = onHide,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.VisibilityOff,
                            contentDescription = "Hide",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }

                // Big Circular Mute Toggle Button
                Surface(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .clickable { onMuteAllToggle() },
                    color = if (isAllMuted) Color(0xFFF43F5E) else themePrimary
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isAllMuted) Icons.Rounded.VolumeOff else Icons.Rounded.VolumeUp,
                            contentDescription = "Mute Toggle",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                // Percentage Indicator
                Text(
                    text = if (isAllMuted) "MUTED (0%)" else "$pct%",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isAllMuted) Color(0xFFF43F5E) else Color.White
                )

                // VERTICAL SLIDER VOLUME BAR
                val trackHeightDp = settings.simpleVolumeLength.heightDp.dp
                val trackWidthDp = 42.dp

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // ▲ + VOL Button
                    Surface(
                        modifier = Modifier
                            .width(trackWidthDp)
                            .height(26.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                val newVol = (primaryStream.currentVolume + 1).coerceAtMost(primaryStream.maxVolume)
                                onVolumeChange(primaryStream.type, newVol)
                            },
                        color = Color(0xFF334155)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "▲",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Vertical Slider Bar Track
                    Box(
                        modifier = Modifier
                            .width(trackWidthDp)
                            .height(trackHeightDp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF1E293B))
                            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                            .pointerInput(primaryStream.maxVolume, primaryStream.currentVolume) {
                                detectTapGestures { offset ->
                                    val heightPx = size.height.toFloat()
                                    if (heightPx > 0) {
                                        val fraction = 1f - (offset.y / heightPx).coerceIn(0f, 1f)
                                        val newVol = (fraction * primaryStream.maxVolume).roundToInt()
                                        onVolumeChange(primaryStream.type, newVol)
                                    }
                                }
                            }
                            .pointerInput(primaryStream.maxVolume, primaryStream.currentVolume) {
                                detectDragGestures { change, _ ->
                                    change.consume()
                                    val heightPx = size.height.toFloat()
                                    if (heightPx > 0) {
                                        val currentY = change.position.y
                                        val fraction = 1f - (currentY / heightPx).coerceIn(0f, 1f)
                                        val newVol = (fraction * primaryStream.maxVolume).roundToInt()
                                        onVolumeChange(primaryStream.type, newVol)
                                    }
                                }
                            },
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        val fillFraction = if (primaryStream.maxVolume > 0) (primaryStream.currentVolume.toFloat() / primaryStream.maxVolume).coerceIn(0f, 1f) else 0f
                        val fillColor = if (isAllMuted || primaryStream.currentVolume == 0) Color(0xFFF43F5E) else themePrimary

                        // Active Volume Level Height Fill
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(fillFraction)
                                .clip(RoundedCornerShape(20.dp))
                                .background(fillColor)
                        )

                        // Vertical Overlay Content
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Icon(
                                imageVector = if (isAllMuted || primaryStream.currentVolume == 0) Icons.Rounded.VolumeOff else Icons.Rounded.VolumeUp,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.size(16.dp)
                            )

                            Text(
                                text = "${(fillFraction * 100).toInt()}%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }

                    // ▼ - VOL Button
                    Surface(
                        modifier = Modifier
                            .width(trackWidthDp)
                            .height(26.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                val newVol = (primaryStream.currentVolume - 1).coerceAtLeast(0)
                                onVolumeChange(primaryStream.type, newVol)
                            },
                        color = Color(0xFF334155)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "▼",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                // Reset 50%
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp)
                        .height(26.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onResetFiftyPercent() },
                    color = themePrimary.copy(alpha = 0.25f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "Reset 50%",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = themePrimary
                        )
                    }
                }
            }
        }
    }

    companion object {
        const val NOTIFICATION_ID = 9981
        const val ACTION_START = "com.reefii.aeromute.START"
        const val ACTION_STOP = "com.reefii.aeromute.STOP"
        const val ACTION_TOGGLE_MUTE = "com.reefii.aeromute.TOGGLE_MUTE"
        const val ACTION_TOGGLE_VISIBILITY = "com.reefii.aeromute.TOGGLE_VISIBILITY"
        const val ACTION_SHOW_OVERLAY = "com.reefii.aeromute.SHOW_OVERLAY"
        const val ACTION_HIDE_OVERLAY = "com.reefii.aeromute.HIDE_OVERLAY"
    }
}

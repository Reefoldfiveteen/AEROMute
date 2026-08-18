package com.reefii.aeromute.ui.screens

import android.content.Context
import android.media.AudioManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DisplaySettings
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.HelpOutline
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Opacity
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Room
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material.icons.rounded.VolumeDown
import androidx.compose.material.icons.rounded.VolumeMute
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reefii.aeromute.data.AeroMutePreferences
import com.reefii.aeromute.data.AeroThemePreset
import com.reefii.aeromute.data.AppLanguage
import com.reefii.aeromute.data.AppStrings
import com.reefii.aeromute.data.AudioStreamManager
import com.reefii.aeromute.data.AudioStreamType
import com.reefii.aeromute.data.DarkModeOption
import com.reefii.aeromute.data.WidgetScale
import com.reefii.aeromute.ui.components.FloatingWidgetPreview
import com.reefii.aeromute.ui.components.PermissionCard

enum class MainNavTab(
    val icon: ImageVector
) {
    UTAMA(Icons.Rounded.Home),
    ADVANCED(Icons.Rounded.Tune),
    GUIDE(Icons.Rounded.MenuBook),
    ABOUT(Icons.Rounded.Info);

    fun getTitle(strings: AppStrings.Strings): String = when (this) {
        UTAMA -> strings.tabHome
        ADVANCED -> strings.tabAdvanced
        GUIDE -> strings.tabGuide
        ABOUT -> strings.tabAbout
    }

    fun getSubtitle(strings: AppStrings.Strings): String = when (this) {
        UTAMA -> strings.tabHomeSub
        ADVANCED -> strings.tabAdvancedSub
        GUIDE -> strings.tabGuideSub
        ABOUT -> strings.tabAboutSub
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AeroMuteMainScreen(
    preferences: AeroMutePreferences,
    isOverlayGranted: Boolean,
    isDndGranted: Boolean,
    onRequestOverlay: () -> Unit,
    onRequestDnd: () -> Unit,
    onToggleService: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val settings by preferences.settings.collectAsState()
    val strings = remember(settings.appLanguage) { AppStrings.get(settings.appLanguage) }
    val audioStreamManager = remember { AudioStreamManager(context) }
    val backupVolumes = remember { mutableMapOf<AudioStreamType, Int>() }

    var selectedTab by remember { mutableStateOf(MainNavTab.UTAMA) }
    var isMutedPreviewState by remember { mutableStateOf(false) }

    // Live volumes in app
    var volumeRefreshTrigger by remember { mutableStateOf(0) }
    val streams = remember(volumeRefreshTrigger, settings) {
        audioStreamManager.getAllStreams(settings)
    }

    val themePrimary = Color(settings.themePreset.primaryColorHex)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 8.dp
            ) {
                MainNavTab.values().forEach { tab ->
                    val isSelected = selectedTab == tab
                    val tabTitle = tab.getTitle(strings)
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tabTitle,
                                tint = if (isSelected) themePrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        label = {
                            Text(
                                text = tabTitle,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) themePrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = themePrimary.copy(alpha = 0.2f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // App Header Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "AEROMute",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = themePrimary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "PRO",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = themePrimary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = selectedTab.getSubtitle(strings),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (settings.isServiceRunning) Color(0xFF10B981).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (settings.isServiceRunning) Color(0xFF10B981).copy(alpha = 0.4f) else Color.Transparent
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (settings.isServiceRunning) Color(0xFF10B981) else Color(0xFF94A3B8))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (settings.isServiceRunning) strings.statusActive else strings.statusInactive,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (settings.isServiceRunning) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Divider(color = MaterialTheme.colorScheme.surfaceVariant)

            // Main Tab View Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when (selectedTab) {
                    MainNavTab.UTAMA -> {
                        MainSettingsView(
                            settings = settings,
                            themePrimary = themePrimary,
                            isOverlayGranted = isOverlayGranted,
                            isDndGranted = isDndGranted,
                            streams = streams,
                            isMutedPreviewState = isMutedPreviewState,
                            onMuteTogglePreview = { isMutedPreviewState = !isMutedPreviewState },
                            onRequestOverlay = onRequestOverlay,
                            onRequestDnd = onRequestDnd,
                            onToggleService = onToggleService,
                            onOpenAdvanced = { selectedTab = MainNavTab.ADVANCED },
                            onOpenGuide = { selectedTab = MainNavTab.GUIDE },
                            onVolumeChange = { type, vol ->
                                audioStreamManager.setStreamVolume(type, vol)
                                volumeRefreshTrigger++
                            },
                            onMuteToggle = { type ->
                                audioStreamManager.toggleStreamMute(type)
                                volumeRefreshTrigger++
                            },
                            onMuteAll = {
                                val muted = audioStreamManager.muteAllActiveStreams(settings, backupVolumes)
                                preferences.updateMuteAll(muted)
                                volumeRefreshTrigger++
                            },
                            onResetFifty = {
                                for (st in streams) {
                                    val vol50 = (st.maxVolume * 0.5f).toInt().coerceAtLeast(1)
                                    audioStreamManager.setStreamVolume(st.type, vol50)
                                }
                                backupVolumes.clear()
                                preferences.updateMuteAll(false)
                                volumeRefreshTrigger++
                            }
                        )
                    }

                    MainNavTab.ADVANCED -> {
                        AdvancedSettingsView(
                            settings = settings,
                            themePrimary = themePrimary,
                            isOverlayGranted = isOverlayGranted,
                            isDndGranted = isDndGranted,
                            onRequestOverlay = onRequestOverlay,
                            onRequestDnd = onRequestDnd,
                            onTransparencyChange = { preferences.updateTransparency(it) },
                            onScaleChange = { preferences.updateWidgetScale(it) },
                            onShapeChange = { preferences.updateWidgetShape(it) },
                            onSimpleVolumeLengthChange = { preferences.updateSimpleVolumeLength(it) },
                            onThemeChange = { preferences.updateThemePreset(it) },
                            onFloatingModeChange = { preferences.updateFloatingMode(it) },
                            onAutoCollapseChange = { preferences.updateAutoCollapse(it) },
                            onSnapToggle = { preferences.updateSnapToEdge(!settings.snapToEdge) },
                            onVibrateToggle = { preferences.updateVibrate(!settings.vibrateOnMute) },
                            onHideOverlayToggle = { preferences.updateOverlayHidden(!settings.isOverlayHidden) },
                            onKeepSilentUnmuteToggle = { preferences.updateKeepSilentVibrateOnUnmute(!settings.keepSilentVibrateOnUnmute) },
                            onResetPosition = { preferences.savePosition(-1, 300) },
                            onVisibilityUpdate = { media, ring, notif, alarm, call ->
                                preferences.updateStreamVisibility(media, ring, notif, alarm, call)
                            },
                            onLanguageChange = { preferences.updateLanguage(it) },
                            onDarkModeChange = { preferences.updateDarkMode(it) }
                        )
                    }

                    MainNavTab.GUIDE -> {
                        UsageGuideView(
                            themePrimary = themePrimary,
                            isServiceRunning = settings.isServiceRunning,
                            onToggleService = onToggleService,
                            appLanguage = settings.appLanguage
                        )
                    }

                    MainNavTab.ABOUT -> {
                        AboutAppView(
                            themePrimary = themePrimary,
                            appLanguage = settings.appLanguage
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 1. TAB UTAMA (PENGATURAN UTAMA)
// ==========================================
@Composable
private fun MainSettingsView(
    settings: com.reefii.aeromute.data.AeroMuteSettings,
    themePrimary: Color,
    isOverlayGranted: Boolean,
    isDndGranted: Boolean,
    streams: List<com.reefii.aeromute.data.StreamVolumeInfo>,
    isMutedPreviewState: Boolean,
    onMuteTogglePreview: () -> Unit,
    onRequestOverlay: () -> Unit,
    onRequestDnd: () -> Unit,
    onToggleService: (Boolean) -> Unit,
    onOpenAdvanced: () -> Unit,
    onOpenGuide: () -> Unit,
    onVolumeChange: (AudioStreamType, Int) -> Unit,
    onMuteToggle: (AudioStreamType) -> Unit,
    onMuteAll: () -> Unit,
    onResetFifty: () -> Unit
) {
    val strings = AppStrings.get(settings.appLanguage)
    val context = LocalContext.current

    var timerEndTimeMillis by remember { mutableStateOf<Long?>(null) }
    var remainingSeconds by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(timerEndTimeMillis) {
        if (timerEndTimeMillis != null) {
            while (true) {
                val now = System.currentTimeMillis()
                val diff = ((timerEndTimeMillis!! - now) / 1000L).coerceAtLeast(0L)
                remainingSeconds = diff
                if (diff <= 0L) {
                    timerEndTimeMillis = null
                    remainingSeconds = null
                    onResetFifty()
                    break
                }
                kotlinx.coroutines.delay(1000L)
            }
        } else {
            remainingSeconds = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Service Banner
        HeroMasterBanner(
            isServiceRunning = settings.isServiceRunning,
            themePrimary = themePrimary,
            isPermissionsGranted = isOverlayGranted && isDndGranted,
            onToggleService = onToggleService,
            appLanguage = settings.appLanguage
        )

        // Permission Card if permissions missing
        if (!isOverlayGranted || !isDndGranted) {
            PermissionCard(
                isOverlayGranted = isOverlayGranted,
                isDndGranted = isDndGranted,
                onRequestOverlay = onRequestOverlay,
                onRequestDnd = onRequestDnd,
                appLanguage = settings.appLanguage
            )
        }

        // Live Widget Preview
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.previewHeader,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = themePrimary,
                        letterSpacing = 1.sp
                    )

                    OutlinedButton(
                        onClick = onOpenAdvanced,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text(text = strings.btnEditDetail, fontSize = 10.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Rounded.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }

                FloatingWidgetPreview(
                    settings = settings,
                    isMutedPreview = isMutedPreviewState,
                    onMuteTogglePreview = onMuteTogglePreview
                )
            }
        }

        // Quick Sound Stream Controllers
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.GraphicEq,
                            contentDescription = null,
                            tint = themePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.volumeControlHeader,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            letterSpacing = 1.sp
                        )
                    }
                }

                // Mute buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val isAllMuted = streams.isNotEmpty() && streams.all { it.isMuted || it.currentVolume == 0 }

                    Button(
                        onClick = onMuteAll,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isAllMuted) Color(0xFF10B981) else Color(0xFFF43F5E)
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = if (isAllMuted) Icons.Rounded.VolumeUp else Icons.Rounded.VolumeOff,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isAllMuted) strings.btnUnmuteAll else strings.btnMuteAll,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Button(
                        onClick = onResetFifty,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = themePrimary.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = strings.btnSet50All,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = themePrimary
                        )
                    }
                }

                Divider(color = MaterialTheme.colorScheme.surfaceVariant)

                // Stream sliders
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    for (stream in streams) {
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
                            Surface(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .clickable { onMuteToggle(stream.type) },
                                color = if (stream.isMuted || stream.currentVolume == 0) Color(0xFFF43F5E).copy(alpha = 0.2f)
                                else themePrimary.copy(alpha = 0.15f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (stream.isMuted || stream.currentVolume == 0) Icons.Rounded.VolumeOff else icon,
                                        contentDescription = stream.type.getLocalizedTitle(strings),
                                        tint = if (stream.isMuted || stream.currentVolume == 0) Color(0xFFF43F5E) else themePrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = stream.type.getLocalizedTitle(strings),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${stream.currentVolume} / ${stream.maxVolume} (${stream.percentage}%)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (stream.isMuted || stream.currentVolume == 0) Color(0xFFF43F5E) else themePrimary
                                    )
                                }

                                Slider(
                                    value = stream.currentVolume.toFloat(),
                                    onValueChange = { onVolumeChange(stream.type, it.toInt()) },
                                    valueRange = 0f..stream.maxVolume.toFloat(),
                                    steps = (stream.maxVolume - 1).coerceAtLeast(0),
                                    colors = SliderDefaults.colors(
                                        thumbColor = if (stream.isMuted || stream.currentVolume == 0) Color(0xFFF43F5E) else themePrimary,
                                        activeTrackColor = if (stream.isMuted || stream.currentVolume == 0) Color(0xFFF43F5E) else themePrimary
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Instant Audio Profiles & Presets
        AudioPresetsCard(
            themePrimary = themePrimary,
            strings = strings,
            onApplyPreset = { mediaPct, ringPct, notifPct, alarmPct, callPct ->
                for (stream in streams) {
                    val pct = when (stream.type) {
                        AudioStreamType.MEDIA -> mediaPct
                        AudioStreamType.RING -> ringPct
                        AudioStreamType.NOTIFICATION -> notifPct
                        AudioStreamType.ALARM -> alarmPct
                        AudioStreamType.CALL -> callPct
                    }
                    val targetVol = (stream.maxVolume * pct).toInt()
                    onVolumeChange(stream.type, targetVol)
                }
            }
        )

        // Timed Quick Mute (Countdown)
        TimedMuteCard(
            themePrimary = themePrimary,
            strings = strings,
            remainingSeconds = remainingSeconds,
            onStartTimer = { minutes ->
                timerEndTimeMillis = System.currentTimeMillis() + (minutes * 60 * 1000L)
                onMuteAll()
            },
            onCancelTimer = {
                timerEndTimeMillis = null
                remainingSeconds = null
                onResetFifty()
            }
        )

        // Live Audio Output Monitor (Option 3)
        AudioDeviceMonitorCard(
            themePrimary = themePrimary,
            strings = strings
        )

        // Audio Connection Safety Guard (Option 2)
        AudioSafetyGuardCard(
            themePrimary = themePrimary,
            strings = strings,
            enabled = settings.autoMuteOnHeadsetDisconnect,
            onToggle = { enabled ->
                AeroMutePreferences.getInstance(context).updateAutoMuteOnHeadsetDisconnect(enabled)
            }
        )

        // Quick Navigation Banner to Panduan
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .clickable { onOpenGuide() },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.HelpOutline,
                        contentDescription = null,
                        tint = themePrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = strings.guideTitle,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = strings.guideSub,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Rounded.ArrowForward,
                    contentDescription = null,
                    tint = themePrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ==========================================
// 2. TAB ADVANCED (OPSI PENGATURAN DETAIL)
// ==========================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AdvancedSettingsView(
    settings: com.reefii.aeromute.data.AeroMuteSettings,
    themePrimary: Color,
    isOverlayGranted: Boolean,
    isDndGranted: Boolean,
    onRequestOverlay: () -> Unit,
    onRequestDnd: () -> Unit,
    onTransparencyChange: (Float) -> Unit,
    onScaleChange: (WidgetScale) -> Unit,
    onShapeChange: (com.reefii.aeromute.data.WidgetShape) -> Unit,
    onSimpleVolumeLengthChange: (com.reefii.aeromute.data.SimpleVolumeLength) -> Unit,
    onThemeChange: (AeroThemePreset) -> Unit,
    onFloatingModeChange: (com.reefii.aeromute.data.FloatingWidgetMode) -> Unit,
    onAutoCollapseChange: (Int) -> Unit,
    onSnapToggle: () -> Unit,
    onVibrateToggle: () -> Unit,
    onHideOverlayToggle: () -> Unit,
    onKeepSilentUnmuteToggle: () -> Unit,
    onResetPosition: () -> Unit,
    onVisibilityUpdate: (Boolean, Boolean, Boolean, Boolean, Boolean) -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
    onDarkModeChange: (DarkModeOption) -> Unit
) {
    val strings = AppStrings.get(settings.appLanguage)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 0. Language & Dark Mode Settings Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Language Selection
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Language,
                        contentDescription = null,
                        tint = themePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.languageHeader,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = 1.sp
                    )
                }

                Text(
                    text = strings.languageSub,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppLanguage.values().forEach { lang ->
                        val isSelected = settings.appLanguage == lang
                        OutlinedButton(
                            onClick = { onLanguageChange(lang) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isSelected) themePrimary else Color.Transparent,
                                contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Text(
                                text = lang.displayName,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                Divider(color = MaterialTheme.colorScheme.surfaceVariant)

                // Dark Mode Selection
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.DarkMode,
                        contentDescription = null,
                        tint = themePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.darkModeHeader,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = 1.sp
                    )
                }

                Text(
                    text = strings.darkModeSub,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DarkModeOption.values().forEach { option ->
                        val isSelected = settings.darkModeOption == option
                        val label = if (settings.appLanguage == AppLanguage.ENGLISH) option.labelEn else option.labelId
                        OutlinedButton(
                            onClick = { onDarkModeChange(option) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isSelected) themePrimary else Color.Transparent,
                                contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Text(
                                text = label,
                                fontSize = 10.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
        // 1. Filter Stream Suara Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.GraphicEq,
                        contentDescription = null,
                        tint = themePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.filterStreamsHeader,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = 1.sp
                    )
                }

                Text(
                    text = strings.filterStreamsSub,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                StreamCheckboxRow(strings.chkMedia, settings.showMediaStream, themePrimary) {
                    onVisibilityUpdate(it, settings.showRingStream, settings.showNotificationStream, settings.showAlarmStream, settings.showCallStream)
                }
                StreamCheckboxRow(strings.chkRing, settings.showRingStream, themePrimary) {
                    onVisibilityUpdate(settings.showMediaStream, it, settings.showNotificationStream, settings.showAlarmStream, settings.showCallStream)
                }
                StreamCheckboxRow(strings.chkNotif, settings.showNotificationStream, themePrimary) {
                    onVisibilityUpdate(settings.showMediaStream, settings.showRingStream, it, settings.showAlarmStream, settings.showCallStream)
                }
                StreamCheckboxRow(strings.chkAlarm, settings.showAlarmStream, themePrimary) {
                    onVisibilityUpdate(settings.showMediaStream, settings.showRingStream, settings.showNotificationStream, it, settings.showCallStream)
                }
                StreamCheckboxRow(strings.chkCall, settings.showCallStream, themePrimary) {
                    onVisibilityUpdate(settings.showMediaStream, settings.showRingStream, settings.showNotificationStream, settings.showAlarmStream, it)
                }
            }
        }

        // Mode Floating Widget Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.DisplaySettings,
                        contentDescription = null,
                        tint = themePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.widgetModeHeader,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = 1.sp
                    )
                }

                Text(
                    text = strings.widgetModeSub,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    com.reefii.aeromute.data.FloatingWidgetMode.values().forEach { mode ->
                        val isSelected = settings.floatingMode == mode
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onFloatingModeChange(mode) }
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) themePrimary else MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            color = if (isSelected) themePrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = mode.getLocalizedLabel(strings),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isSelected) themePrimary else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = mode.getLocalizedDescription(strings),
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Transparansi & Ukuran Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Opacity,
                        contentDescription = null,
                        tint = themePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.opacityHeader,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = 1.sp
                    )
                }

                Divider(color = MaterialTheme.colorScheme.surfaceVariant)

                // Transparansi Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = strings.opacityLabel,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = themePrimary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "${(settings.transparency * 100).toInt()}% Opacity",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = themePrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Slider(
                        value = settings.transparency,
                        onValueChange = onTransparencyChange,
                        valueRange = 0.2f..1.0f,
                        steps = 15,
                        colors = SliderDefaults.colors(
                            thumbColor = themePrimary,
                            activeTrackColor = themePrimary
                        )
                    )
                }

                // Widget Scale
                Column {
                    Text(
                        text = strings.widgetScaleLabel,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        WidgetScale.values().forEach { scale ->
                            val isSelected = settings.widgetScale == scale
                            OutlinedButton(
                                onClick = { onScaleChange(scale) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isSelected) themePrimary else Color.Transparent,
                                    contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            ) {
                                Text(
                                    text = scale.getLocalizedLabel(settings.appLanguage),
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Widget Shape Selection
                Column {
                    Text(
                        text = strings.widgetShapeLabel,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        com.reefii.aeromute.data.WidgetShape.values().forEach { shape ->
                            val isSelected = settings.widgetShape == shape
                            OutlinedButton(
                                onClick = { onShapeChange(shape) },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isSelected) themePrimary else Color.Transparent,
                                    contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            ) {
                                Text(
                                    text = shape.getLocalizedLabel(settings.appLanguage),
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Simple Mode Volume Bar Height
                Column {
                    Text(
                        text = strings.simpleVolumeLengthLabel,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        com.reefii.aeromute.data.SimpleVolumeLength.values().forEach { len ->
                            val isSelected = settings.simpleVolumeLength == len
                            OutlinedButton(
                                onClick = { onSimpleVolumeLengthChange(len) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isSelected) themePrimary else Color.Transparent,
                                    contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            ) {
                                Text(
                                    text = len.getLocalizedLabel(settings.appLanguage),
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Theme Presets
                Column {
                    Text(
                        text = strings.colorThemeLabel,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AeroThemePreset.values().forEach { preset ->
                            val isSelected = settings.themePreset == preset
                            val presetColor = Color(preset.primaryColorHex)

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) presetColor else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.clickable { onThemeChange(preset) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(presetColor)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = preset.displayName,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Behavior Options Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Tune,
                        contentDescription = null,
                        tint = themePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.behaviorHeader,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = 1.sp
                    )
                }

                Divider(color = MaterialTheme.colorScheme.surfaceVariant)

                // Auto Collapse Timer
                Column {
                    Text(
                        text = strings.behaviorAutoCollapse,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val timerOptions = listOf(0 to strings.optionOff, 3 to "3s", 5 to "5s", 10 to "10s")
                        timerOptions.forEach { (sec, label) ->
                            val isSelected = settings.autoCollapseSeconds == sec
                            OutlinedButton(
                                onClick = { onAutoCollapseChange(sec) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isSelected) themePrimary else Color.Transparent,
                                    contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                // Snap to edge switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Room,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.behaviorSnapEdge,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Switch(
                        checked = settings.snapToEdge,
                        onCheckedChange = { onSnapToggle() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = themePrimary
                        )
                    )
                }

                // Vibration feedback switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Vibration,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.behaviorVibrate,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Switch(
                        checked = settings.vibrateOnMute,
                        onCheckedChange = { onVibrateToggle() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = themePrimary
                        )
                    )
                }

                // Hide Floating Widget switch
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.VisibilityOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = strings.behaviorHideOverlay,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Switch(
                            checked = settings.isOverlayHidden,
                            onCheckedChange = { onHideOverlayToggle() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = themePrimary
                            )
                        )
                    }
                    Text(
                        text = strings.behaviorHideOverlayDesc,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 26.dp, top = 2.dp)
                    )
                }

                // Keep in Silent/Vibrate on Unmute switch
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.VolumeOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = strings.behaviorKeepSilentUnmute,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Switch(
                            checked = settings.keepSilentVibrateOnUnmute,
                            onCheckedChange = { onKeepSilentUnmuteToggle() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = themePrimary
                            )
                        )
                    }
                    Text(
                        text = strings.behaviorKeepSilentUnmuteDesc,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 26.dp, top = 2.dp)
                    )
                }

                OutlinedButton(
                    onClick = onResetPosition,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.RestartAlt,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = strings.btnResetPos,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // 4. System Permissions Status Card
        PermissionCard(
            isOverlayGranted = isOverlayGranted,
            isDndGranted = isDndGranted,
            onRequestOverlay = onRequestOverlay,
            onRequestDnd = onRequestDnd,
            appLanguage = settings.appLanguage
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ==========================================
// 3. TAB PANDUAN (WINDOW CARA PENGGUNAAN)
// ==========================================
@Composable
private fun UsageGuideView(
    themePrimary: Color,
    isServiceRunning: Boolean,
    onToggleService: (Boolean) -> Unit,
    appLanguage: AppLanguage = AppLanguage.ENGLISH
) {
    val strings = AppStrings.get(appLanguage)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Header Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = themePrimary)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = strings.guideTitle,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
                Text(
                    text = strings.guideSub,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = strings.guideWelcomeDesc,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }

        // Visual Gesture Cards
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = strings.guideGesturesTitle,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = themePrimary,
                    letterSpacing = 1.sp
                )

                Divider(color = MaterialTheme.colorScheme.surfaceVariant)

                GestureGuideRow(
                    icon = Icons.Rounded.TouchApp,
                    title = strings.gesture1Title,
                    description = strings.gesture1Desc,
                    themePrimary = themePrimary
                )

                GestureGuideRow(
                    icon = Icons.Rounded.VolumeUp,
                    title = strings.gesture2Title,
                    description = strings.gesture2Desc,
                    themePrimary = themePrimary
                )

                GestureGuideRow(
                    icon = Icons.Rounded.VolumeOff,
                    title = strings.gesture3Title,
                    description = strings.gesture3Desc,
                    themePrimary = themePrimary
                )

                GestureGuideRow(
                    icon = Icons.Rounded.DisplaySettings,
                    title = strings.gesture4Title,
                    description = strings.gesture4Desc,
                    themePrimary = themePrimary
                )
            }
        }

        // Quick Settings Tile Guide Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Layers,
                        contentDescription = null,
                        tint = themePrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.guideQuickTileTitle,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = 1.sp
                    )
                }

                Text(
                    text = strings.guideQuickTileDesc,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    TileStepItem(strings.tileStep1, strings.tileStep1Desc, themePrimary)
                    TileStepItem(strings.tileStep2, strings.tileStep2Desc, themePrimary)
                    TileStepItem(strings.tileStep3, strings.tileStep3Desc, themePrimary)
                    TileStepItem(strings.tileStep4, strings.tileStep4Desc, themePrimary)
                }

                // Interactive Tile Preview
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (isServiceRunning) themePrimary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.VolumeOff,
                                        contentDescription = null,
                                        tint = if (isServiceRunning) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "AEROMute Quick Tile",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isServiceRunning) strings.tileStatusReady else strings.tileStatusInactive,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Button(
                            onClick = { onToggleService(!isServiceRunning) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isServiceRunning) Color(0xFF10B981) else themePrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                        ) {
                            Text(
                                text = if (isServiceRunning) strings.tileTestMute else strings.tileTestActivate,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ==========================================
// SUB-COMPONENTS
// ==========================================
@Composable
private fun GestureGuideRow(
    icon: ImageVector,
    title: String,
    description: String,
    themePrimary: Color = MaterialTheme.colorScheme.primary
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape),
            color = themePrimary.copy(alpha = 0.15f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = themePrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TileStepItem(
    step: String,
    text: String,
    themePrimary: Color = MaterialTheme.colorScheme.primary
) {
    Row(verticalAlignment = Alignment.Top) {
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = themePrimary.copy(alpha = 0.15f)
        ) {
            Text(
                text = step,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = themePrimary,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AudioPresetsCard(
    themePrimary: Color,
    strings: AppStrings.Strings,
    onApplyPreset: (mediaPct: Float, ringPct: Float, notifPct: Float, alarmPct: Float, callPct: Float) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Tune,
                    contentDescription = null,
                    tint = themePrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = strings.presetHeader,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = 1.sp
                )
            }

            Text(
                text = strings.presetSub,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Game Mode
                PresetChip(
                    label = strings.presetGame,
                    icon = Icons.Rounded.GraphicEq,
                    themePrimary = themePrimary,
                    onClick = { onApplyPreset(0.8f, 0f, 0f, 1f, 0.5f) }
                )
                // Silent Mode
                PresetChip(
                    label = strings.presetSilent,
                    icon = Icons.Rounded.VolumeOff,
                    themePrimary = themePrimary,
                    onClick = { onApplyPreset(0f, 0f, 0f, 1f, 0f) }
                )
                // Outdoor Mode
                PresetChip(
                    label = strings.presetOutdoor,
                    icon = Icons.Rounded.VolumeUp,
                    themePrimary = themePrimary,
                    onClick = { onApplyPreset(1f, 1f, 1f, 1f, 1f) }
                )
                // Night Mode
                PresetChip(
                    label = strings.presetNight,
                    icon = Icons.Rounded.DarkMode,
                    themePrimary = themePrimary,
                    onClick = { onApplyPreset(0f, 0.1f, 0f, 1f, 0.2f) }
                )
                // Media Focus
                PresetChip(
                    label = strings.presetMediaFocus,
                    icon = Icons.Rounded.MusicNote,
                    themePrimary = themePrimary,
                    onClick = { onApplyPreset(0.75f, 0f, 0f, 1f, 0f) }
                )
            }
        }
    }
}

@Composable
private fun PresetChip(
    label: String,
    icon: ImageVector,
    themePrimary: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = themePrimary.copy(alpha = 0.12f),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = themePrimary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = themePrimary
            )
        }
    }
}

@Composable
private fun TimedMuteCard(
    themePrimary: Color,
    strings: AppStrings.Strings,
    remainingSeconds: Long?,
    onStartTimer: (minutes: Int) -> Unit,
    onCancelTimer: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Alarm,
                    contentDescription = null,
                    tint = themePrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = strings.timerHeader,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = 1.sp
                )
            }

            Text(
                text = strings.timerSub,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (remainingSeconds != null && remainingSeconds > 0) {
                // Active Countdown View
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = themePrimary.copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = strings.timerActive,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        val mins = remainingSeconds / 60
                        val secs = remainingSeconds % 60
                        val formattedTime = String.format("%02d:%02d", mins, secs)
                        Text(
                            text = formattedTime,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = themePrimary
                        )
                        Button(
                            onClick = onCancelTimer,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF43F5E)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = strings.btnCancelTimer,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } else {
                // Timer Preset Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val timerOptions = listOf(
                        15 to strings.timer15m,
                        30 to strings.timer30m,
                        60 to strings.timer1h,
                        120 to strings.timer2h
                    )
                    timerOptions.forEach { (mins, label) ->
                        OutlinedButton(
                            onClick = { onStartTimer(mins) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = themePrimary
                            )
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroMasterBanner(
    isServiceRunning: Boolean,
    themePrimary: Color,
    isPermissionsGranted: Boolean,
    onToggleService: (Boolean) -> Unit,
    appLanguage: AppLanguage = AppLanguage.ENGLISH
) {
    val strings = AppStrings.get(appLanguage)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .shadow(8.dp, RoundedCornerShape(28.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = if (isServiceRunning) listOf(
                            themePrimary,
                            Color(0xFF1E1B4B)
                        ) else listOf(
                            Color(0xFF334155),
                            Color(0xFF0F172A)
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "FLOATING VOLUME & QUICK MUTE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "AEROMute",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = if (isServiceRunning) strings.heroActiveDesc else strings.heroInactiveDesc,
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }

                    // Power Master Switch Button
                    Surface(
                        modifier = Modifier
                            .size(60.dp)
                            .shadow(8.dp, CircleShape)
                            .clip(CircleShape)
                            .clickable { onToggleService(!isServiceRunning) },
                        color = if (isServiceRunning) Color(0xFF10B981) else Color(0xFFF43F5E)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.PowerSettingsNew,
                                contentDescription = "Toggle Service",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatusChip(
                        label = if (isServiceRunning) strings.statusServiceActive else strings.statusServiceInactive,
                        isSuccess = isServiceRunning,
                        modifier = Modifier.weight(1f)
                    )
                    StatusChip(
                        label = if (isPermissionsGranted) strings.statusPermGranted else strings.statusPermNeeded,
                        isSuccess = isPermissionsGranted,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusChip(
    label: String,
    isSuccess: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color.Black.copy(alpha = 0.25f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isSuccess) Color(0xFF10B981) else Color(0xFFF59E0B))
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun StreamCheckboxRow(
    label: String,
    checked: Boolean,
    themePrimary: Color = MaterialTheme.colorScheme.primary,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(checkedColor = themePrimary)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun AudioDeviceMonitorCard(
    themePrimary: Color,
    strings: AppStrings.Strings
) {
    val context = LocalContext.current
    var audioDeviceState by remember { mutableStateOf("Speaker") }

    LaunchedEffect(Unit) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? android.media.AudioManager
        if (audioManager != null) {
            audioDeviceState = when {
                audioManager.isBluetoothA2dpOn || audioManager.isBluetoothScoOn -> "Bluetooth"
                audioManager.isWiredHeadsetOn -> "Headset"
                else -> "Speaker"
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.GraphicEq,
                    contentDescription = null,
                    tint = themePrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = strings.audioDeviceHeader,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = 1.sp
                )
            }

            Text(
                text = strings.audioDeviceSub,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = when (audioDeviceState) {
                    "Bluetooth" -> Color(0xFF06B6D4).copy(alpha = 0.12f)
                    "Headset" -> Color(0xFF10B981).copy(alpha = 0.12f)
                    else -> themePrimary.copy(alpha = 0.12f)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val icon = when (audioDeviceState) {
                            "Bluetooth" -> Icons.Rounded.Bluetooth
                            "Headset" -> Icons.Rounded.Headphones
                            else -> Icons.Rounded.VolumeUp
                        }
                        val iconColor = when (audioDeviceState) {
                            "Bluetooth" -> Color(0xFF06B6D4)
                            "Headset" -> Color(0xFF10B981)
                            else -> themePrimary
                        }
                        Surface(
                            shape = CircleShape,
                            color = iconColor.copy(alpha = 0.2f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = iconColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = strings.audioDeviceActive,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            val label = when (audioDeviceState) {
                                "Bluetooth" -> strings.audioDeviceBluetooth
                                "Headset" -> strings.audioDeviceHeadset
                                else -> strings.audioDeviceSpeaker
                            }
                            Text(
                                text = label,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AudioSafetyGuardCard(
    themePrimary: Color,
    strings: AppStrings.Strings,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Shield,
                    contentDescription = null,
                    tint = themePrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = strings.safetyHeader,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = 1.sp
                )
            }

            Text(
                text = strings.safetySub,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = strings.safetyToggleLabel,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = strings.safetyToggleDesc,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Switch(
                    checked = enabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = themePrimary
                    )
                )
            }
        }
    }
}

// ==========================================
// 4. TAB TENTANG (ABOUT APP)
// ==========================================
@Composable
private fun AboutAppView(
    themePrimary: Color,
    appLanguage: com.reefii.aeromute.data.AppLanguage
) {
    val strings = remember(appLanguage) { com.reefii.aeromute.data.AppStrings.get(appLanguage) }
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
    val githubUrl = "https://github.com/Reefoldfiveteen/AEROMute"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero App Header Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = themePrimary.copy(alpha = 0.15f),
                    modifier = Modifier.size(72.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.VolumeUp,
                            contentDescription = null,
                            tint = themePrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = strings.aboutAppTitle,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = themePrimary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = strings.aboutAppVersion,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = themePrimary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = strings.aboutAppTagline,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        // Key Capabilities / Features Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = strings.aboutFeaturesTitle,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = 1.sp
                )

                AboutFeatureRow(
                    icon = Icons.Rounded.Layers,
                    title = strings.aboutFeature1Title,
                    desc = strings.aboutFeature1Desc,
                    themePrimary = themePrimary
                )
                AboutFeatureRow(
                    icon = Icons.Rounded.VolumeOff,
                    title = strings.aboutFeature2Title,
                    desc = strings.aboutFeature2Desc,
                    themePrimary = themePrimary
                )
                AboutFeatureRow(
                    icon = Icons.Rounded.Shield,
                    title = strings.aboutFeature3Title,
                    desc = strings.aboutFeature3Desc,
                    themePrimary = themePrimary
                )
                AboutFeatureRow(
                    icon = Icons.Rounded.Settings,
                    title = strings.aboutFeature4Title,
                    desc = strings.aboutFeature4Desc,
                    themePrimary = themePrimary
                )
            }
        }

        // Developer & Engine Info Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = strings.aboutDevTitle,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = 1.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = themePrimary.copy(alpha = 0.15f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Info,
                                contentDescription = null,
                                tint = themePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = strings.aboutDevName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = strings.aboutDevRole,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // GitHub Repository Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = strings.aboutGithubTitle,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = 1.sp
                )

                Text(
                    text = strings.aboutGithubDesc,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Button(
                    onClick = {
                        try {
                            uriHandler.openUri(githubUrl)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = themePrimary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.aboutGithubBtn,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Privacy & Security Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Shield,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.aboutPrivacyTitle,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = 1.sp
                    )
                }
                Text(
                    text = strings.aboutPrivacyDesc,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun AboutFeatureRow(
    icon: ImageVector,
    title: String,
    desc: String,
    themePrimary: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = themePrimary.copy(alpha = 0.12f),
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = themePrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = desc,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

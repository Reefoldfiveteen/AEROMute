package com.reefii.aeromute.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class FloatingWidgetMode(val value: Int, val label: String, val description: String) {
    STANDARD(1, "Standar", "Panel slider per-stream"),
    SIMPLE(2, "Simpel", "Bar vertikal ringkas dengan mute & persen");

    companion object {
        fun fromValue(value: Int): FloatingWidgetMode = values().find { it.value == value } ?: STANDARD
    }
}

enum class WidgetScale(val value: Int, val label: String, val sizeDp: Int) {
    SMALL(1, "Ring / Kecil", 46),
    MEDIUM(2, "Sedang", 58),
    LARGE(3, "Besar", 70);

    companion object {
        fun fromValue(value: Int): WidgetScale = values().find { it.value == value } ?: MEDIUM
    }
}

enum class AeroThemePreset(val id: String, val displayName: String, val primaryColorHex: Long) {
    INDIGO("Indigo", "Indigo Aero", 0xFF6366F1),
    CYAN("Cyan", "Cyber Cyan", 0xFF06B6D4),
    EMERALD("Emerald", "Neon Emerald", 0xFF10B981),
    ROSE("Rose", "Sunset Rose", 0xFFF43F5E),
    AMOLED("AMOLED", "Dark Minimal", 0xFF0F172A);

    companion object {
        fun fromId(id: String): AeroThemePreset = values().find { it.id == id } ?: INDIGO
    }
}

data class AeroMuteSettings(
    val isServiceRunning: Boolean = false,
    val transparency: Float = 0.88f,
    val widgetScale: WidgetScale = WidgetScale.MEDIUM,
    val themePreset: AeroThemePreset = AeroThemePreset.INDIGO,
    val floatingMode: FloatingWidgetMode = FloatingWidgetMode.STANDARD,
    val autoCollapseSeconds: Int = 5,
    val showMediaStream: Boolean = true,
    val showRingStream: Boolean = true,
    val showNotificationStream: Boolean = true,
    val showAlarmStream: Boolean = true,
    val showCallStream: Boolean = false,
    val isMutedAll: Boolean = false,
    val savedX: Int = -1,
    val savedY: Int = 300,
    val isLocked: Boolean = false,
    val vibrateOnMute: Boolean = true,
    val snapToEdge: Boolean = true
)

class AeroMutePreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("aeromute_prefs", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<AeroMuteSettings> = _settings.asStateFlow()

    private fun loadSettings(): AeroMuteSettings {
        return AeroMuteSettings(
            isServiceRunning = prefs.getBoolean(KEY_SERVICE_RUNNING, false),
            transparency = prefs.getFloat(KEY_TRANSPARENCY, 0.88f),
            widgetScale = WidgetScale.fromValue(prefs.getInt(KEY_WIDGET_SCALE, WidgetScale.MEDIUM.value)),
            themePreset = AeroThemePreset.fromId(prefs.getString(KEY_THEME_PRESET, AeroThemePreset.INDIGO.id) ?: AeroThemePreset.INDIGO.id),
            floatingMode = FloatingWidgetMode.fromValue(prefs.getInt(KEY_FLOATING_MODE, FloatingWidgetMode.STANDARD.value)),
            autoCollapseSeconds = prefs.getInt(KEY_AUTO_COLLAPSE, 5),
            showMediaStream = prefs.getBoolean(KEY_SHOW_MEDIA, true),
            showRingStream = prefs.getBoolean(KEY_SHOW_RING, true),
            showNotificationStream = prefs.getBoolean(KEY_SHOW_NOTIF, true),
            showAlarmStream = prefs.getBoolean(KEY_SHOW_ALARM, true),
            showCallStream = prefs.getBoolean(KEY_SHOW_CALL, false),
            isMutedAll = prefs.getBoolean(KEY_IS_MUTED_ALL, false),
            savedX = prefs.getInt(KEY_SAVED_X, -1),
            savedY = prefs.getInt(KEY_SAVED_Y, 300),
            isLocked = prefs.getBoolean(KEY_IS_LOCKED, false),
            vibrateOnMute = prefs.getBoolean(KEY_VIBRATE, true),
            snapToEdge = prefs.getBoolean(KEY_SNAP_EDGE, true)
        )
    }

    fun updateServiceState(isRunning: Boolean) {
        prefs.edit().putBoolean(KEY_SERVICE_RUNNING, isRunning).apply()
        _settings.value = _settings.value.copy(isServiceRunning = isRunning)
    }

    fun updateTransparency(value: Float) {
        val clamped = value.coerceIn(0.15f, 1.0f)
        prefs.edit().putFloat(KEY_TRANSPARENCY, clamped).apply()
        _settings.value = _settings.value.copy(transparency = clamped)
    }

    fun updateWidgetScale(scale: WidgetScale) {
        prefs.edit().putInt(KEY_WIDGET_SCALE, scale.value).apply()
        _settings.value = _settings.value.copy(widgetScale = scale)
    }

    fun updateThemePreset(preset: AeroThemePreset) {
        prefs.edit().putString(KEY_THEME_PRESET, preset.id).apply()
        _settings.value = _settings.value.copy(themePreset = preset)
    }

    fun updateFloatingMode(mode: FloatingWidgetMode) {
        prefs.edit().putInt(KEY_FLOATING_MODE, mode.value).apply()
        _settings.value = _settings.value.copy(floatingMode = mode)
    }

    fun updateAutoCollapse(seconds: Int) {
        prefs.edit().putInt(KEY_AUTO_COLLAPSE, seconds).apply()
        _settings.value = _settings.value.copy(autoCollapseSeconds = seconds)
    }

    fun updateStreamVisibility(
        media: Boolean = _settings.value.showMediaStream,
        ring: Boolean = _settings.value.showRingStream,
        notif: Boolean = _settings.value.showNotificationStream,
        alarm: Boolean = _settings.value.showAlarmStream,
        call: Boolean = _settings.value.showCallStream
    ) {
        prefs.edit()
            .putBoolean(KEY_SHOW_MEDIA, media)
            .putBoolean(KEY_SHOW_RING, ring)
            .putBoolean(KEY_SHOW_NOTIF, notif)
            .putBoolean(KEY_SHOW_ALARM, alarm)
            .putBoolean(KEY_SHOW_CALL, call)
            .apply()

        _settings.value = _settings.value.copy(
            showMediaStream = media,
            showRingStream = ring,
            showNotificationStream = notif,
            showAlarmStream = alarm,
            showCallStream = call
        )
    }

    fun updateMuteAll(muted: Boolean) {
        prefs.edit().putBoolean(KEY_IS_MUTED_ALL, muted).apply()
        _settings.value = _settings.value.copy(isMutedAll = muted)
    }

    fun savePosition(x: Int, y: Int) {
        prefs.edit().putInt(KEY_SAVED_X, x).putInt(KEY_SAVED_Y, y).apply()
        _settings.value = _settings.value.copy(savedX = x, savedY = y)
    }

    fun updateLocked(locked: Boolean) {
        prefs.edit().putBoolean(KEY_IS_LOCKED, locked).apply()
        _settings.value = _settings.value.copy(isLocked = locked)
    }

    fun updateVibrate(vibrate: Boolean) {
        prefs.edit().putBoolean(KEY_VIBRATE, vibrate).apply()
        _settings.value = _settings.value.copy(vibrateOnMute = vibrate)
    }

    fun updateSnapToEdge(snap: Boolean) {
        prefs.edit().putBoolean(KEY_SNAP_EDGE, snap).apply()
        _settings.value = _settings.value.copy(snapToEdge = snap)
    }

    companion object {
        private const val KEY_SERVICE_RUNNING = "service_running"
        private const val KEY_TRANSPARENCY = "transparency"
        private const val KEY_WIDGET_SCALE = "widget_scale"
        private const val KEY_THEME_PRESET = "theme_preset"
        private const val KEY_FLOATING_MODE = "floating_mode"
        private const val KEY_AUTO_COLLAPSE = "auto_collapse"
        private const val KEY_SHOW_MEDIA = "show_media"
        private const val KEY_SHOW_RING = "show_ring"
        private const val KEY_SHOW_NOTIF = "show_notif"
        private const val KEY_SHOW_ALARM = "show_alarm"
        private const val KEY_SHOW_CALL = "show_call"
        private const val KEY_IS_MUTED_ALL = "is_muted_all"
        private const val KEY_SAVED_X = "saved_x"
        private const val KEY_SAVED_Y = "saved_y"
        private const val KEY_IS_LOCKED = "is_locked"
        private const val KEY_VIBRATE = "vibrate_on_mute"
        private const val KEY_SNAP_EDGE = "snap_to_edge"

        @Volatile
        private var INSTANCE: AeroMutePreferences? = null

        fun getInstance(context: Context): AeroMutePreferences {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AeroMutePreferences(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}

package com.reefii.aeromute.data

import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper

enum class AudioStreamType(
    val streamId: Int,
    val title: String,
    val iconName: String
) {
    MEDIA(AudioManager.STREAM_MUSIC, "Media", "music_note"),
    RING(AudioManager.STREAM_RING, "Ring", "notifications"),
    NOTIFICATION(AudioManager.STREAM_NOTIFICATION, "Notification", "notifications_active"),
    ALARM(AudioManager.STREAM_ALARM, "Alarm", "alarm"),
    CALL(AudioManager.STREAM_VOICE_CALL, "Telepon", "call");

    fun getLocalizedTitle(strings: AppStrings.Strings): String = when (this) {
        MEDIA -> strings.streamMedia
        RING -> strings.streamRing
        NOTIFICATION -> strings.streamNotification
        ALARM -> strings.streamAlarm
        CALL -> strings.streamCall
    }
}

data class StreamVolumeInfo(
    val type: AudioStreamType,
    val currentVolume: Int,
    val maxVolume: Int,
    val isMuted: Boolean
) {
    val percentage: Int
        get() = if (maxVolume > 0) ((currentVolume.toFloat() / maxVolume) * 100).toInt() else 0
}

class AudioStreamManager(private val context: Context) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    fun getStreamInfo(type: AudioStreamType): StreamVolumeInfo {
        val current = try {
            audioManager.getStreamVolume(type.streamId)
        } catch (e: Exception) {
            0
        }
        val max = try {
            audioManager.getStreamMaxVolume(type.streamId)
        } catch (e: Exception) {
            15
        }
        val isMuted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                audioManager.isStreamMute(type.streamId) || current == 0
            } catch (e: Exception) {
                current == 0
            }
        } else {
            current == 0
        }

        return StreamVolumeInfo(type, current, max, isMuted)
    }

    fun getAllStreams(settings: AeroMuteSettings): List<StreamVolumeInfo> {
        val list = mutableListOf<StreamVolumeInfo>()
        if (settings.showMediaStream) list.add(getStreamInfo(AudioStreamType.MEDIA))
        if (settings.showRingStream) list.add(getStreamInfo(AudioStreamType.RING))
        if (settings.showNotificationStream) list.add(getStreamInfo(AudioStreamType.NOTIFICATION))
        if (settings.showAlarmStream) list.add(getStreamInfo(AudioStreamType.ALARM))
        if (settings.showCallStream) list.add(getStreamInfo(AudioStreamType.CALL))
        return list
    }

    fun setStreamVolume(type: AudioStreamType, volume: Int) {
        try {
            val max = audioManager.getStreamMaxVolume(type.streamId)
            val clamped = volume.coerceIn(0, max)
            if (clamped == 0 && (type == AudioStreamType.RING || type == AudioStreamType.NOTIFICATION)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    try {
                        audioManager.adjustStreamVolume(type.streamId, AudioManager.ADJUST_MUTE, 0)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                forceSilentMode()
            } else {
                audioManager.setStreamVolume(type.streamId, clamped, 0)
                if (clamped > 0 && (type == AudioStreamType.RING || type == AudioStreamType.NOTIFICATION)) {
                    if (audioManager.ringerMode != AudioManager.RINGER_MODE_NORMAL) {
                        try {
                            audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun forceSilentMode() {
        try {
            audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun toggleStreamMute(type: AudioStreamType): Boolean {
        val info = getStreamInfo(type)
        return if (info.isMuted || info.currentVolume == 0) {
            // Unmute - restore to saved or 50% max
            val defaultVol = (info.maxVolume * 0.5f).toInt().coerceAtLeast(1)
            val prefs = AeroMutePreferences.getInstance(context)
            val restoreVol = prefs.getBackupVolume(type.streamId, defaultVol)
            setStreamVolume(type, restoreVol)
            false
        } else {
            // Mute - save backup and set to 0
            val prefs = AeroMutePreferences.getInstance(context)
            if (info.currentVolume > 0) {
                prefs.saveBackupVolume(type.streamId, info.currentVolume)
            }
            setStreamVolume(type, 0)
            true
        }
    }

    fun muteAllActiveStreams(
        settings: AeroMuteSettings,
        backupVolumes: MutableMap<AudioStreamType, Int>,
        excludeAlarm: Boolean = false
    ): Boolean {
        val streams = getAllStreams(settings).filter { if (excludeAlarm) it.type != AudioStreamType.ALARM else true }
        val mediaStream = streams.firstOrNull { it.type == AudioStreamType.MEDIA } ?: getStreamInfo(AudioStreamType.MEDIA)
        
        // If media is muted (0) or all active streams are 0, we treat current state as MUTED -> so we perform UNMUTE
        val isCurrentlyMuted = mediaStream.currentVolume == 0 || isAllStreamsMuted(settings, excludeAlarm)
        val prefs = AeroMutePreferences.getInstance(context)

        if (isCurrentlyMuted) {
            // Unmute & Restore volumes
            for (stream in streams) {
                if (settings.keepSilentVibrateOnUnmute && (stream.type == AudioStreamType.RING || stream.type == AudioStreamType.NOTIFICATION)) {
                    continue
                }
                val inMemBackup = backupVolumes[stream.type]
                val defaultVol = (stream.maxVolume * 0.5f).toInt().coerceAtLeast(1)
                val restore = if (inMemBackup != null && inMemBackup > 0) {
                    inMemBackup
                } else {
                    prefs.getBackupVolume(stream.type.streamId, defaultVol)
                }
                setStreamVolume(stream.type, restore)
            }
            if (settings.keepSilentVibrateOnUnmute) {
                forceSilentMode()
            } else {
                try {
                    audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            backupVolumes.clear()
            return false // now unmuted
        } else {
            // Mute all active streams
            for (stream in streams) {
                if (stream.currentVolume > 0) {
                    backupVolumes[stream.type] = stream.currentVolume
                    prefs.saveBackupVolume(stream.type.streamId, stream.currentVolume)
                }
                if (stream.type == AudioStreamType.RING || stream.type == AudioStreamType.NOTIFICATION) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        try {
                            audioManager.adjustStreamVolume(stream.type.streamId, AudioManager.ADJUST_MUTE, 0)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } else {
                    setStreamVolume(stream.type, 0)
                }
            }
            forceSilentMode()
            return true // now muted
        }
    }

    fun isMediaMuted(): Boolean {
        val media = getStreamInfo(AudioStreamType.MEDIA)
        return media.isMuted || media.currentVolume == 0
    }

    fun isAnyStreamMuted(settings: AeroMuteSettings): Boolean {
        return getAllStreams(settings).any { it.isMuted || it.currentVolume == 0 }
    }

    fun isAllStreamsMuted(settings: AeroMuteSettings, excludeAlarm: Boolean = false): Boolean {
        val streams = getAllStreams(settings).filter { if (excludeAlarm) it.type != AudioStreamType.ALARM else true }
        return streams.isNotEmpty() && streams.all { it.isMuted || it.currentVolume == 0 }
    }
}

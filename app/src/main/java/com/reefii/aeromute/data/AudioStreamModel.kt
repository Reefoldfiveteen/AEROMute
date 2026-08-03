package com.reefii.aeromute.data

import android.content.Context
import android.media.AudioManager
import android.os.Build

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
            audioManager.setStreamVolume(type.streamId, clamped, 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun toggleStreamMute(type: AudioStreamType): Boolean {
        val info = getStreamInfo(type)
        return if (info.isMuted || info.currentVolume == 0) {
            // Unmute - set to 50% max
            val restoreVol = (info.maxVolume * 0.5f).toInt().coerceAtLeast(1)
            setStreamVolume(type, restoreVol)
            false
        } else {
            // Mute - set to 0
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
        val currentlyMuted = isAllStreamsMuted(settings, excludeAlarm)

        if (currentlyMuted) {
            // Restore volume for streams
            for (stream in streams) {
                val backup = backupVolumes[stream.type]
                val restore = if (backup != null && backup > 0) backup else (stream.maxVolume * 0.5f).toInt().coerceAtLeast(1)
                setStreamVolume(stream.type, restore)
            }
            backupVolumes.clear()
            return false // now unmuted
        } else {
            // Mute all active streams
            for (stream in streams) {
                if (stream.currentVolume > 0) {
                    backupVolumes[stream.type] = stream.currentVolume
                }
                setStreamVolume(stream.type, 0)
            }
            return true // now muted
        }
    }

    fun isAnyStreamMuted(settings: AeroMuteSettings): Boolean {
        return getAllStreams(settings).any { it.isMuted || it.currentVolume == 0 }
    }

    fun isAllStreamsMuted(settings: AeroMuteSettings, excludeAlarm: Boolean = false): Boolean {
        val streams = getAllStreams(settings).filter { if (excludeAlarm) it.type != AudioStreamType.ALARM else true }
        return streams.isNotEmpty() && streams.all { it.isMuted || it.currentVolume == 0 }
    }
}

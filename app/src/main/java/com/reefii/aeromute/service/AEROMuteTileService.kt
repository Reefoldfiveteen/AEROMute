package com.reefii.aeromute.service

import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.reefii.aeromute.data.AeroMutePreferences
import com.reefii.aeromute.data.AudioStreamManager

class AEROMuteTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()
        val prefs = AeroMutePreferences.getInstance(applicationContext)
        val isRunning = prefs.settings.value.isServiceRunning

        if (!isRunning) {
            // Start Floating Service
            val intent = Intent(this, FloatingMuteService::class.java).apply {
                action = FloatingMuteService.ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
            prefs.updateServiceState(true)
        } else {
            // Toggle Mute All via Quick Tile
            val audioManager = AudioStreamManager(applicationContext)
            val isMuted = audioManager.isAllStreamsMuted(prefs.settings.value)
            audioManager.muteAllActiveStreams(prefs.settings.value, mutableMapOf())
            prefs.updateMuteAll(!isMuted)
        }

        updateTileState()
    }

    private fun updateTileState() {
        val tile = qsTile ?: return
        val prefs = AeroMutePreferences.getInstance(applicationContext)
        val isRunning = prefs.settings.value.isServiceRunning
        val audioManager = AudioStreamManager(applicationContext)
        val isMuted = audioManager.isAllStreamsMuted(prefs.settings.value)

        if (!isRunning) {
            tile.state = Tile.STATE_INACTIVE
            tile.label = "AEROMute"
            tile.subtitle = "Mati (Ketuk utk Aktifkan)"
        } else {
            tile.state = Tile.STATE_ACTIVE
            if (isMuted) {
                tile.label = "AEROMute"
                tile.subtitle = "MUTE (Aktif)"
            } else {
                tile.label = "AEROMute"
                tile.subtitle = "Suara Aktif"
            }
        }
        tile.updateTile()
    }
}

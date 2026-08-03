package com.reefii.aeromute.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class VolumeChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "android.media.VOLUME_CHANGED_ACTION") {
            // Send broadcast to update FloatingMuteService overlay view
            context?.let { ctx ->
                val updateIntent = Intent(ACTION_VOLUME_UPDATED).apply {
                    setPackage(ctx.packageName)
                }
                ctx.sendBroadcast(updateIntent)
            }
        }
    }

    companion object {
        const val ACTION_VOLUME_UPDATED = "com.reefii.aeromute.VOLUME_UPDATED"
    }
}

package com.jirani.app.sync

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.jirani.app.R
import com.jirani.app.data.local.LocalFirstUiStore

class RelayForegroundService : Service() {
    override fun onCreate() {
        super.onCreate()
        createChannel()
        LocalFirstUiStore.initializePersistence(applicationContext)
        NearbySyncRuntime.initialize(applicationContext)
        NearbySyncRuntime.ensureAvailable()
        startForeground(NotificationId, notification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        NearbySyncRuntime.ensureAvailable()
        return START_STICKY
    }

    override fun onDestroy() {
        if (!LocalFirstUiStore.isActiveRelayModeEnabled()) {
            NearbySyncRuntime.stop()
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createChannel() {
        if (Build.VERSION.SDK_INT < 26) return
        val manager = getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            ChannelId,
            "Jirani active relay",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "Keeps Jirani available for explicit nearby relay mode."
        }
        manager.createNotificationChannel(channel)
    }

    private fun notification(): Notification =
        NotificationCompat.Builder(this, ChannelId)
            .setSmallIcon(R.drawable.ic_mesh_status)
            .setContentTitle("Jirani relay is active")
            .setContentText("Nearby relay is available while active relay mode is on.")
            .setOngoing(true)
            .setShowWhen(false)
            .build()

    private companion object {
        const val ChannelId = "jirani_active_relay"
        const val NotificationId = 42
    }
}

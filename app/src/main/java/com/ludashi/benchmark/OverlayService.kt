package com.ludashi.benchmark

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

/**
 * Foreground service that keeps the com.ludashi.benchmark process alive.
 *
 * OEM performance boost systems on Chinese devices (Xiaomi, vivo/iQOO, OPPO/OnePlus,
 * Realme, etc.) detect benchmark apps by scanning running processes/packages and apply
 * a higher power/thermal profile when they see known benchmark package names.
 *
 * By running as a foreground service, this process stays resident and visible to the
 * OEM's detection layer while the user freely uses any other app.
 */
class OverlayService : Service() {

    companion object {
        const val ACTION_STOP = "com.ludashi.benchmark.ACTION_STOP"
        private const val CHANNEL_ID = "overlay_service_channel"
        private const val NOTIFICATION_ID = 1001

        @Volatile
        var isRunning = false
    }

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        startForegroundWithNotification()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Keep service alive if killed by system
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startForegroundWithNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.channel_description)
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val stopIntent = Intent(this, OverlayReceiver::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = androidx.core.app.NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_close_clear_cancel)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_text))
            .setOngoing(true)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_LOW)
            .addAction(
                android.R.drawable.ic_delete,
                getString(R.string.action_stop),
                stopPendingIntent
            )
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
    }
}

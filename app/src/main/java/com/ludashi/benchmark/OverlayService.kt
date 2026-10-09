package com.ludashi.benchmark

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat

/**
 * Foreground service that keeps a transparent TYPE_APPLICATION_OVERLAY window
 * registered in the system's WindowManager under com.ludashi.benchmark.
 *
 * Why this works:
 * OEM benchmark detection (Xiaomi, vivo/iQOO, OPPO/Realme, etc.) scans either:
 *   (a) running process list for known benchmark package names, or
 *   (b) WindowManager's window list for benchmark package windows
 *
 * A foreground service alone satisfies (a). The overlay window satisfies (b)
 * and crucially persists even when the user switches to another app — because
 * TYPE_APPLICATION_OVERLAY windows are independent of the activity stack.
 *
 * The window is 100% transparent and FLAG_NOT_TOUCHABLE + FLAG_NOT_TOUCH_MODAL,
 * so the user experiences zero visual or interaction difference.
 */
class OverlayService : Service() {

    companion object {
        const val ACTION_STOP = "com.ludashi.benchmark.ACTION_STOP"
        private const val CHANNEL_ID = "overlay_service_channel"
        private const val NOTIFICATION_ID = 1001

        @Volatile
        var isRunning = false
    }

    private var overlayView: View? = null
    private var windowManager: WindowManager? = null

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        addOverlayWindow()
        startForegroundWithNotification()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    /**
     * Adds a full-screen transparent window via TYPE_APPLICATION_OVERLAY.
     * This is the key registration in WindowManager that OEM detectors scan.
     */
    private fun addOverlayWindow() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        overlayView = View(this).apply {
            setBackgroundColor(Color.TRANSPARENT)
            isClickable = false
            isFocusable = false
        }

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )

        windowManager?.addView(overlayView, params)
    }

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

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_close_clear_cancel)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_text))
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
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
        overlayView?.let { windowManager?.removeView(it) }
        overlayView = null
    }
}

package com.ludashi.benchmark

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat

class OverlayActivity : Activity() {

    companion object {
        const val ACTION_STOP_OVERLAY = "com.ludashi.benchmark.ACTION_STOP"
        private const val CHANNEL_ID = "overlay_service_channel"
        private const val NOTIFICATION_ID = 1001

        /**
         * Global reference to current instance to allow instantaneous dismiss
         * from notification receiver or external intent without overhead.
         */
        @Volatile
        var currentInstance: OverlayActivity? = null

        /**
         * Touch event passthrough flag.
         * When true, FLAG_NOT_TOUCHABLE is applied so all taps and gestures pass
         * directly to whatever app or screen is underneath.
         */
        const val PASS_TOUCHES_THROUGH = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentInstance = this

        // 1. Configure 100% transparent edge-to-edge window
        setupTransparentWindow()

        // 2. Set touch pass-through flags so touches go to the underlying screen
        applyTouchFlags()

        // 3. Post a dismiss notification in shade (since touches fall through)
        showControlNotification()

        // 4. Ultra-minimal transparent root view (0 CPU / 0 GPU processing)
        val emptyView = View(this).apply {
            setBackgroundColor(Color.TRANSPARENT)
            isClickable = false
            isFocusable = false
        }
        setContentView(emptyView)
    }

    private fun setupTransparentWindow() {
        // Transparent window background
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        // Full screen edge-to-edge layout
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            )
        }

        // Fill entire screen
        window.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
    }

    private fun applyTouchFlags() {
        if (PASS_TOUCHES_THROUGH) {
            // FLAG_NOT_TOUCHABLE: touches go straight to whatever window is behind this activity
            // FLAG_NOT_TOUCH_MODAL: touches outside the window also pass through
            window.addFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            )
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        // If triggered again with stop flag or via launcher icon re-tap, finish overlay
        if (intent?.getBooleanExtra("STOP_OVERLAY", false) == true) {
            finish()
        }
    }

    private fun showControlNotification() {
        try {
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
                action = ACTION_STOP_OVERLAY
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
                .setContentIntent(stopPendingIntent)
                .build()

            notificationManager.notify(NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
            // Handled gracefully on Android 13+ if notification permission not granted
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (currentInstance == this) {
            currentInstance = null
        }
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notificationManager?.cancel(NOTIFICATION_ID)
    }
}

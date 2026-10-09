package com.ludashi.benchmark

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.view.View
import android.view.WindowManager
import android.widget.Toast

/**
 * Transparent activity that immediately enters Picture-in-Picture mode.
 *
 * WHY PiP?
 * OEM benchmark boost systems (Xiaomi, vivo/iQOO, OPPO, Realme, etc.) detect
 * benchmarks by watching ActivityManager for foreground activity package names.
 * A background service or overlay window does NOT satisfy this check.
 *
 * A PiP activity is special: Android keeps it registered as a FOREGROUND component
 * in ActivityManager even while the user actively uses another app. The OEM's
 * detection layer sees com.ludashi.benchmark as a foreground activity at all times.
 *
 * The PiP window is configured to the smallest allowed ratio and is fully
 * transparent — essentially invisible to the user.
 */
class OverlayActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Toggle off if already running
        if (OverlayService.isRunning) {
            stopService(Intent(this, OverlayService::class.java))
            Toast.makeText(this, "Benchmark spoof stopped", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupTransparentWindow()

        // Minimal transparent content — zero rendering cost
        val emptyView = View(this).apply {
            setBackgroundColor(Color.TRANSPARENT)
            isClickable = false
            isFocusable = false
        }
        setContentView(emptyView)

        // Start the foreground service — keeps process alive if PiP is dismissed
        val serviceIntent = Intent(this, OverlayService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }

        Toast.makeText(this, "Benchmark spoof active", Toast.LENGTH_SHORT).show()

        // Enter PiP immediately — this is what keeps us as a foreground activity
        // while the user freely switches to and uses any other app
        enterPipMode()
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode)
        if (!isInPictureInPictureMode) {
            // User dismissed the PiP window — stop everything
            stopService(Intent(this, OverlayService::class.java))
            finish()
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        // Re-enter PiP whenever user presses Home/Recents
        enterPipMode()
    }

    private fun enterPipMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val params = PictureInPictureParams.Builder()
                // Smallest allowed aspect ratio — makes PiP window as tiny as possible
                .setAspectRatio(Rational(1, 1))
                .build()
            enterPictureInPictureMode(params)
        } else {
            // Android < 8 fallback: just move to back (PiP not available)
            moveTaskToBack(true)
        }
    }

    private fun setupTransparentWindow() {
        window.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(Color.TRANSPARENT))
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        window.addFlags(
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        }
        window.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
    }
}

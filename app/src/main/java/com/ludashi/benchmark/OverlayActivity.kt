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

class OverlayActivity : Activity() {

    private var hasStartedPip = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (OverlayService.isRunning) {
            stopService(Intent(this, OverlayService::class.java))
            Toast.makeText(this, "Benchmark spoof stopped", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupTransparentWindow()

        val emptyView = View(this).apply {
            setBackgroundColor(Color.TRANSPARENT)
            isClickable = false
            isFocusable = false
        }
        setContentView(emptyView)

        val serviceIntent = Intent(this, OverlayService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }

        Toast.makeText(this, "Benchmark spoof active", Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        // Wait until onResume to enter PiP so the activity has a valid state
        if (!hasStartedPip && !isInPictureInPictureMode) {
            enterPipMode()
            hasStartedPip = true
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode)
        if (!isInPictureInPictureMode) {
            stopService(Intent(this, OverlayService::class.java))
            finish()
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (!isInPictureInPictureMode) {
            enterPipMode()
        }
    }

    private fun enterPipMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val paramsBuilder = PictureInPictureParams.Builder()
                .setAspectRatio(Rational(1, 1))
                
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                paramsBuilder.setAutoEnterEnabled(true)
            }
            
            try {
                enterPictureInPictureMode(paramsBuilder.build())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
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

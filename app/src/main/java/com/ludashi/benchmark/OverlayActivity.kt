package com.ludashi.benchmark

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast

/**
 * Permission gate + toggle.
 *
 * First launch: requests SYSTEM_ALERT_WINDOW if not granted, then starts service.
 * Subsequent taps: stops the service.
 *
 * The activity finishes/backgrounds immediately — the foreground service +
 * overlay window keep the spoof active while the user uses other apps.
 */
class OverlayActivity : Activity() {

    companion object {
        private const val REQUEST_OVERLAY_PERMISSION = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Toggle off if already running
        if (OverlayService.isRunning) {
            stopService(Intent(this, OverlayService::class.java))
            Toast.makeText(this, "Benchmark spoof stopped", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        if (Settings.canDrawOverlays(this)) {
            startSpoof()
        } else {
            // Ask for "Display over other apps" permission
            Toast.makeText(
                this,
                "Grant \"Display over other apps\" then tap the icon again",
                Toast.LENGTH_LONG
            ).show()
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            @Suppress("DEPRECATION")
            startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_OVERLAY_PERMISSION) {
            if (Settings.canDrawOverlays(this)) {
                startSpoof()
            } else {
                Toast.makeText(
                    this,
                    "Permission denied — cannot start spoof",
                    Toast.LENGTH_LONG
                ).show()
            }
            finish()
        }
    }

    private fun startSpoof() {
        val intent = Intent(this, OverlayService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        Toast.makeText(this, "Benchmark spoof active", Toast.LENGTH_SHORT).show()
        // Go to background — spoof keeps running via service
        moveTaskToBack(true)
    }
}

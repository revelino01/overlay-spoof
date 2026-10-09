package com.ludashi.benchmark

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast

/**
 * Launcher activity that acts as a permission gate.
 * - Checks for SYSTEM_ALERT_WINDOW permission
 * - If granted: starts OverlayService and finishes immediately
 * - If not granted: sends user to Settings to enable "Display over other apps"
 */
class OverlayActivity : Activity() {

    companion object {
        private const val REQUEST_OVERLAY_PERMISSION = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (isOverlayServiceRunning()) {
            // Tapping the icon again stops the overlay
            stopService(Intent(this, OverlayService::class.java))
            Toast.makeText(this, "Overlay stopped", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        if (Settings.canDrawOverlays(this)) {
            startOverlayService()
            finish()
        } else {
            requestOverlayPermission()
        }
    }

    private fun requestOverlayPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        @Suppress("DEPRECATION")
        startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_OVERLAY_PERMISSION) {
            if (Settings.canDrawOverlays(this)) {
                startOverlayService()
            } else {
                Toast.makeText(
                    this,
                    "Overlay permission is required for this app to work",
                    Toast.LENGTH_LONG
                ).show()
            }
            finish()
        }
    }

    private fun startOverlayService() {
        val serviceIntent = Intent(this, OverlayService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
        Toast.makeText(this, "Overlay active", Toast.LENGTH_SHORT).show()
    }

    private fun isOverlayServiceRunning(): Boolean {
        return OverlayService.isRunning
    }
}

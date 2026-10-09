package com.ludashi.benchmark

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast

/**
 * Launcher activity. Starts the OverlayService to keep the benchmark process
 * alive in the background, then immediately moves itself to the back so the
 * user can freely use other apps while the OEM sees com.ludashi.benchmark running.
 *
 * Tap again to stop.
 */
class OverlayActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (OverlayService.isRunning) {
            // Second tap — stop the service
            stopService(Intent(this, OverlayService::class.java))
            Toast.makeText(this, "Benchmark spoof stopped", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            // Start the foreground service to keep process alive
            val serviceIntent = Intent(this, OverlayService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
            Toast.makeText(this, "Benchmark spoof active", Toast.LENGTH_SHORT).show()
            // Move to back so user can use other apps — service keeps process alive
            moveTaskToBack(true)
        }
    }
}

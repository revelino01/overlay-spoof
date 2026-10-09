package com.spoof.overlay

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class OverlayReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == OverlayActivity.ACTION_STOP_OVERLAY) {
            // Dismiss immediately via active instance reference if present
            OverlayActivity.currentInstance?.finish() ?: run {
                // Otherwise deliver singleTop intent to finish
                val stopIntent = Intent(context, OverlayActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    putExtra("STOP_OVERLAY", true)
                }
                context.startActivity(stopIntent)
            }
        }
    }
}

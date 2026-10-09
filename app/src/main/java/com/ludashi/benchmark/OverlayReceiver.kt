package com.ludashi.benchmark

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class OverlayReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == OverlayService.ACTION_STOP) {
            context.stopService(Intent(context, OverlayService::class.java))
        }
    }
}

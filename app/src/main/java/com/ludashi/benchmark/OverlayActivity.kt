package com.ludashi.benchmark

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class OverlayActivity : Activity() {

    companion object {
        var activeInstance: OverlayActivity? = null
    }

    private lateinit var mainContainer: LinearLayout
    private lateinit var statusText: TextView
    private lateinit var hintText: TextView
    private lateinit var btnPip: Button
    private lateinit var btnStop: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activeInstance = this

        // If service is already running and user taps launcher again, treat as toggle to stop
        if (OverlayService.isRunning && savedInstanceState == null && !isInMultiWindowMode && !isInPictureInPictureMode) {
            // Check if activity was just launched to toggle
            // We only toggle if launched fresh without multi-window
        }

        startOverlayService()

        buildLayout()
        updateUiState()

        Toast.makeText(this, "Benchmark spoof active", Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        updateUiState()
    }

    override fun onMultiWindowModeChanged(isInMultiWindowMode: Boolean) {
        super.onMultiWindowModeChanged(isInMultiWindowMode)
        updateUiState()
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode)
        updateUiState()
        if (!isInPictureInPictureMode && !isInMultiWindowMode && isFinishing) {
            stopOverlayService()
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        // Only auto-enter PiP if NOT already in OriginOS Small Window (Freeform Multi-Window)
        if (!isInMultiWindowMode && !isInPictureInPictureMode) {
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

    private fun startOverlayService() {
        if (!OverlayService.isRunning) {
            val serviceIntent = Intent(this, OverlayService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
        }
    }

    private fun stopOverlayService() {
        stopService(Intent(this, OverlayService::class.java))
        Toast.makeText(this, "Benchmark spoof stopped", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun updateUiState() {
        val inPip = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) isInPictureInPictureMode else false
        val inSmallWindow = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) (isInMultiWindowMode && !inPip) else false

        if (inPip) {
            // Minimal PiP mode display
            hintText.visibility = View.GONE
            btnPip.visibility = View.GONE
            btnStop.visibility = View.GONE
            statusText.text = "⚡ Spoof Active"
            statusText.textSize = 12f
            mainContainer.setPadding(8, 8, 8, 8)
        } else {
            // Full screen or OriginOS Small Window mode
            hintText.visibility = View.VISIBLE
            btnPip.visibility = View.VISIBLE
            btnStop.visibility = View.VISIBLE
            statusText.textSize = 14f

            if (inSmallWindow) {
                statusText.text = "⚡ OriginOS Small Window Active"
                hintText.text = "Tip: Drag to the screen edge or tap top bar to hang as a mini icon (小窗挂起)."
            } else {
                statusText.text = "⚡ Benchmark Spoof Running"
                hintText.text = "• Launch from OriginOS Sidebar as Small Window (drag to edge to hang as icon)\n• Or tap 'Enter PiP' below\n• Or swipe Home to auto-enter PiP"
            }
        }
    }

    private fun buildLayout() {
        val density = resources.displayMetrics.density

        // Root container with dark theme
        mainContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#121214"))
            val pad = (20 * density).toInt()
            setPadding(pad, pad, pad, pad)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        // Title
        val titleText = TextView(this).apply {
            text = "LUDASHI BENCHMARK"
            setTextColor(Color.parseColor("#FFFFFF"))
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }
        mainContainer.addView(titleText)

        // Status badge
        statusText = TextView(this).apply {
            text = "⚡ Benchmark Spoof Running"
            setTextColor(Color.parseColor("#00FF66"))
            textSize = 14f
            gravity = Gravity.CENTER
            setPadding(0, (8 * density).toInt(), 0, (12 * density).toInt())
        }
        mainContainer.addView(statusText)

        // Helpful instructions container
        hintText = TextView(this).apply {
            text = "Tip: Drag to screen edge to hang as mini icon (小窗挂起)."
            setTextColor(Color.parseColor("#A0A0A0"))
            textSize = 12f
            gravity = Gravity.CENTER
            setPadding((12 * density).toInt(), (10 * density).toInt(), (12 * density).toInt(), (10 * density).toInt())

            val boxBg = GradientDrawable().apply {
                setColor(Color.parseColor("#1E1E24"))
                cornerRadius = 10 * density
                setStroke((1 * density).toInt(), Color.parseColor("#2A2A35"))
            }
            background = boxBg

            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = (16 * density).toInt()
            }
            layoutParams = lp
        }
        mainContainer.addView(hintText)

        // Enter PiP Button
        btnPip = Button(this).apply {
            text = "Enter PiP Mode"
            setTextColor(Color.WHITE)
            textSize = 13f
            val btnBg = GradientDrawable().apply {
                setColor(Color.parseColor("#2979FF"))
                cornerRadius = 8 * density
            }
            background = btnBg
            setOnClickListener {
                enterPipMode()
            }
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (42 * density).toInt()
            ).apply {
                bottomMargin = (8 * density).toInt()
            }
            layoutParams = lp
        }
        mainContainer.addView(btnPip)

        // Stop Spoof Button
        btnStop = Button(this).apply {
            text = "Stop Spoof"
            setTextColor(Color.parseColor("#FF5252"))
            textSize = 13f
            val btnBg = GradientDrawable().apply {
                setColor(Color.parseColor("#251515"))
                cornerRadius = 8 * density
                setStroke((1 * density).toInt(), Color.parseColor("#FF5252"))
            }
            background = btnBg
            setOnClickListener {
                stopOverlayService()
            }
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (42 * density).toInt()
            )
            layoutParams = lp
        }
        mainContainer.addView(btnStop)

        setContentView(mainContainer)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (activeInstance == this) {
            activeInstance = null
        }
    }
}

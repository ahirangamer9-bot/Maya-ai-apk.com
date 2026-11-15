package com.example.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.example.MainActivity
import com.example.R
import com.example.controller.PhoneController

class FloatingAssistantService : Service() {

    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private var isExpanded: Boolean = false

    companion object {
        var isServiceRunning: Boolean = false
            private set
    }

    override fun onBind(intent: Intent?): IBinder? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate() {
        super.onCreate()
        isServiceRunning = true
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 30
            y = 200
        }

        // Programmatically build the floating assistant view with bubble and popup menu
        val rootLayout = FrameLayout(this)

        // 1. Collapsed Circular Avatar Bubble
        val bubble = ImageView(this).apply {
            setImageResource(R.drawable.maya_core_orb)
            layoutParams = FrameLayout.LayoutParams(140, 140)
            elevation = 16f
        }

        // 2. Expanded Control Panel Layout
        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundResource(R.drawable.bg_maya_widget)
            setPadding(24, 20, 24, 20)
            visibility = View.GONE
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val titleView = TextView(this).apply {
            text = "Maya Live Assistant ✨"
            setTextColor(android.graphics.Color.WHITE)
            textSize = 14f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        panel.addView(titleView)

        // Actions Row
        val actionsRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 16, 0, 10)
        }

        // Quick Torch Button
        val btnTorch = TextView(this).apply {
            text = "🔦 Flash"
            setTextColor(android.graphics.Color.WHITE)
            setBackgroundColor(android.graphics.Color.parseColor("#059669"))
            setPadding(20, 12, 20, 12)
            textSize = 12f
            setOnClickListener {
                PhoneController.toggleTorch(context)
                PhoneController.triggerHaptic(context, 60)
            }
        }
        actionsRow.addView(btnTorch)

        // Quick App Launcher
        val btnOpen = TextView(this).apply {
            text = "🚀 Open App"
            setTextColor(android.graphics.Color.WHITE)
            setBackgroundColor(android.graphics.Color.parseColor("#2563EB"))
            setPadding(20, 12, 20, 12)
            textSize = 12f
            setOnClickListener {
                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                startActivity(intent)
                panel.visibility = View.GONE
                bubble.visibility = View.VISIBLE
                isExpanded = false
            }
        }
        actionsRow.addView(btnOpen)

        // Close Floating Button
        val btnClose = TextView(this).apply {
            text = "✕ Close"
            setTextColor(android.graphics.Color.WHITE)
            setBackgroundColor(android.graphics.Color.parseColor("#DC2626"))
            setPadding(20, 12, 20, 12)
            textSize = 12f
            setOnClickListener {
                stopSelf()
            }
        }
        actionsRow.addView(btnClose)

        panel.addView(actionsRow)

        rootLayout.addView(panel)
        rootLayout.addView(bubble)

        // Drag and Click Listener for the bubble
        bubble.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f
            private var isClick = true

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        isClick = true
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = (event.rawX - initialTouchX).toInt()
                        val dy = (event.rawY - initialTouchY).toInt()
                        if (Math.abs(dx) > 10 || Math.abs(dy) > 10) {
                            isClick = false
                        }
                        params.x = initialX + dx
                        params.y = initialY + dy
                        windowManager?.updateViewLayout(rootLayout, params)
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        if (isClick) {
                            isExpanded = !isExpanded
                            if (isExpanded) {
                                panel.visibility = View.VISIBLE
                                bubble.visibility = View.GONE
                            } else {
                                panel.visibility = View.GONE
                                bubble.visibility = View.VISIBLE
                            }
                        }
                        return true
                    }
                }
                return false
            }
        })

        floatingView = rootLayout
        windowManager?.addView(rootLayout, params)
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
        floatingView?.let { windowManager?.removeView(it) }
        floatingView = null
    }
}

package com.example.myapp
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.os.Build
import android.os.IBinder
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import androidx.core.app.NotificationCompat
import kotlin.math.abs

class PoolModService : Service() {
    private lateinit var windowManager: WindowManager
    private lateinit var overlayCanvasView: DirectLineCanvasView
    private lateinit var floatingModButton: Button
    private var isModActive = true

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        createNotificationChannel()
        val notification = NotificationCompat.Builder(this, "POOL_MOD_CHANNEL")
            .setContentTitle("8 Ball Pool MOD Floating Active")
            .setContentText("Guidelines & MOD Floating Control...")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()
        startForeground(1, notification)

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }

        // Fullscreen Guidelines Overlay Window
        val canvasParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )
        canvasParams.gravity = Gravity.TOP or Gravity.START

        overlayCanvasView = DirectLineCanvasView(this)
        windowManager.addView(overlayCanvasView, canvasParams)

        // Floating MOD Control Button Widget
        val buttonParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        buttonParams.gravity = Gravity.TOP or Gravity.START
        buttonParams.x = 100
        buttonParams.y = 250

        floatingModButton = Button(this).apply {
            text = "MOD: ON"
            setBackgroundColor(Color.parseColor("#DD000000"))
            setTextColor(Color.GREEN)
            textSize = 14f
            setPadding(30, 20, 30, 20)
        }

        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f

        floatingModButton.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = buttonParams.x
                    initialY = buttonParams.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    buttonParams.x = initialX + (event.rawX - initialTouchX).toInt()
                    buttonParams.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager.updateViewLayout(floatingModButton, buttonParams)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val diffX = Math.abs(event.rawX - initialTouchX)
                    val diffY = Math.abs(event.rawY - initialTouchY)
                    if (diffX < 10 && diffY < 10) {
                        isModActive = !isModActive
                        overlayCanvasView.visibility = if (isModActive) View.VISIBLE else View.GONE
                        floatingModButton.text = if (isModActive) "MOD: ON" else "MOD: OFF"
                        floatingModButton.setTextColor(if (isModActive) Color.GREEN else Color.RED)
                    }
                    true
                }
                else -> false
            }
        }

        windowManager.addView(floatingModButton, buttonParams)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "POOL_MOD_CHANNEL",
                "Pool Mod Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        if (::overlayCanvasView.isInitialized) windowManager.removeView(overlayCanvasView)
        if (::floatingModButton.isInitialized) windowManager.removeView(floatingModButton)
    }
}

class DirectLineCanvasView(context: Context) : View(context) {

    private val mainAimPaint = Paint().apply {
        color = Color.WHITE
        strokeWidth = 7f
        isAntiAlias = true
        style = Paint.Style.STROKE
    }

    private val reflectionPaint = Paint().apply {
        color = Color.rgb(255, 140, 0)
        strokeWidth = 6f
        isAntiAlias = true
        style = Paint.Style.STROKE
    }

    private val cushionPaint = Paint().apply {
        color = Color.rgb(186, 85, 211)
        strokeWidth = 5f
        isAntiAlias = true
        style = Paint.Style.STROKE
    }

    private val targetHitPaint = Paint().apply {
        color = Color.YELLOW
        strokeWidth = 5f
        isAntiAlias = true
        style = Paint.Style.STROKE
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()

        if (w <= 0 || h <= 0) return

        // Always render visible extended guidelines matching table dimensions directly
        val startX = w * 0.32f
        val startY = h * 0.52f
        val hitX = w * 0.73f
        val hitY = h * 0.52f

        // 1. Extended White Main Cue Line
        canvas.drawLine(startX, startY, hitX, hitY, mainAimPaint)

        // 2. Target Hit Circle Point
        canvas.drawCircle(hitX, hitY, h * 0.028f, targetHitPaint)

        // 3. Orange Target Ball Reflection Path
        canvas.drawLine(hitX, hitY, w * 0.88f, h * 0.32f, reflectionPaint)

        // 4. Purple Cue Ball Cushion Reflection Path
        canvas.drawLine(hitX, hitY, w * 0.88f, h * 0.78f, cushionPaint)
        canvas.drawLine(w * 0.88f, h * 0.78f, w * 0.62f, h * 0.85f, cushionPaint)

        invalidate() // Continuous Rendering Loop
    }
}

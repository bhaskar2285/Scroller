package com.scrollbot.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import com.scrollbot.data.AppTarget
import com.scrollbot.results.ResultsActivity

class FloatingService : Service() {

    private lateinit var windowManager: WindowManager
    private var floatingView: ComposeView? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForeground(1, buildNotification())
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        addFloatingView()
    }

    private fun addFloatingView() {
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = 16; y = 200
        }

        floatingView = ComposeView(this).apply {
            setContent {
                FloatingView { query, target -> launchScan(query, target) }
            }
        }
        windowManager.addView(floatingView, params)
    }

    private fun launchScan(query: String, target: AppTarget) {
        val intent = Intent(this, ResultsActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra("query", query)
            putExtra("target", target.name)
        }
        startActivity(intent)
    }

    override fun onDestroy() {
        floatingView?.let { windowManager.removeView(it) }
        super.onDestroy()
    }

    private fun buildNotification(): Notification {
        val channelId = "scrollbot_service"
        val channel = NotificationChannel(channelId, "ScrollBot", NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        return Notification.Builder(this, channelId)
            .setContentTitle("ScrollBot active")
            .setSmallIcon(android.R.drawable.ic_menu_search)
            .build()
    }
}

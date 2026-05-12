package com.scrollbot

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import com.scrollbot.onboarding.ModelDownloadManager
import com.scrollbot.onboarding.OnboardingActivity
import com.scrollbot.overlay.FloatingService

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val overlayOk = Settings.canDrawOverlays(this)
        val modelOk = ModelDownloadManager(this).isModelReady

        if (overlayOk && modelOk) {
            startService(Intent(this, FloatingService::class.java))
            finish()
        } else {
            startActivity(Intent(this, OnboardingActivity::class.java))
            finish()
        }
    }
}

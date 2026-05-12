package com.scrollbot.onboarding

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.scrollbot.overlay.FloatingService
import kotlinx.coroutines.launch

class OnboardingActivity : ComponentActivity() {

    private val downloader by lazy { ModelDownloadManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var step by remember { mutableStateOf(0) }
            var downloadProgress by remember { mutableStateOf(0f) }
            var downloadMessage by remember { mutableStateOf("") }

            OnboardingScreen(
                step = step,
                downloadProgress = downloadProgress,
                downloadMessage = downloadMessage,
                onGrantOverlay = {
                    startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")))
                },
                onGrantAccessibility = {
                    startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                },
                onDownloadModel = {
                    step = 3
                    lifecycleScope.launch {
                        downloader.downloadAll { progress, msg ->
                            downloadProgress = progress
                            downloadMessage = msg
                            if (progress >= 1f) step = 4
                        }
                    }
                },
                onFinish = {
                    startService(Intent(this, FloatingService::class.java))
                    finish()
                },
                onCheckPermissions = {
                    val overlayOk = Settings.canDrawOverlays(this)
                    val modelOk = downloader.isModelReady
                    if (overlayOk && modelOk) step = 4
                    else if (overlayOk) step = 2
                    else step = 1
                }
            )
        }
    }
}

@Composable
fun OnboardingScreen(
    step: Int,
    downloadProgress: Float,
    downloadMessage: String,
    onGrantOverlay: () -> Unit,
    onGrantAccessibility: () -> Unit,
    onDownloadModel: () -> Unit,
    onFinish: () -> Unit,
    onCheckPermissions: () -> Unit
) {
    LaunchedEffect(Unit) { onCheckPermissions() }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("ScrollBot", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(8.dp))
        Text("AI-powered scrolling assistant", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(40.dp))

        when (step) {
            0, 1 -> {
                Text("Step 1/3: Allow overlay permission")
                Spacer(Modifier.height(16.dp))
                Button(onClick = onGrantOverlay) { Text("Grant Overlay Permission") }
            }
            2 -> {
                Text("Step 2/3: Enable Accessibility Service")
                Spacer(Modifier.height(8.dp))
                Text("Find 'ScrollBot' in Accessibility settings and enable it.",
                    style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(16.dp))
                Button(onClick = onGrantAccessibility) { Text("Open Accessibility Settings") }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = onDownloadModel) { Text("I've enabled it → Next") }
            }
            3 -> {
                Text("Step 3/3: Downloading AI model (~1.1GB)")
                Spacer(Modifier.height(16.dp))
                LinearProgressIndicator(progress = { downloadProgress }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                Text(downloadMessage, style = MaterialTheme.typography.bodySmall)
            }
            4 -> {
                Text("All set!")
                Spacer(Modifier.height(8.dp))
                Text("The floating button will appear on your screen.")
                Spacer(Modifier.height(16.dp))
                Button(onClick = onFinish) { Text("Start ScrollBot") }
            }
        }
    }
}

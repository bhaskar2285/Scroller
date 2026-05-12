package com.scrollbot.results

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.scrollbot.ScanOrchestrator
import com.scrollbot.capture.ScreenCaptureManager
import com.scrollbot.data.AppTarget
import com.scrollbot.data.RankedItem
import kotlinx.coroutines.launch

class ResultsActivity : ComponentActivity() {

    private val captureManager by lazy { ScreenCaptureManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val query = intent.getStringExtra("query") ?: ""
        val target = AppTarget.valueOf(intent.getStringExtra("target") ?: "LAZADA")

        setContent {
            var results by remember { mutableStateOf<List<RankedItem>>(emptyList()) }
            var progressMessage by remember { mutableStateOf("Starting scan...") }
            var done by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                lifecycleScope.launch {
                    val orchestrator = ScanOrchestrator(this@ResultsActivity, captureManager)
                    val items = orchestrator.scan(query, target) { progress ->
                        progressMessage = progress.message
                        if (progress.stage == ScanOrchestrator.Stage.DONE) done = true
                    }
                    results = items
                    done = true
                }
            }

            ResultsScreen(
                query = query,
                target = target,
                results = results,
                progressMessage = progressMessage,
                done = done,
                onOpenItem = { item -> openDeepLink(item) },
                onBack = { finish() }
            )
        }
    }

    private fun openDeepLink(item: RankedItem) {
        val url = item.deepLink ?: return
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }
}

@Composable
fun ResultsScreen(
    query: String,
    target: AppTarget,
    results: List<RankedItem>,
    progressMessage: String,
    done: Boolean,
    onOpenItem: (RankedItem) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("← Back", color = Color(0xFF0A84FF)) }
            Spacer(Modifier.width(8.dp))
            Text(
                "\"$query\" on ${target.name}",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(8.dp))

        if (!done) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF0A84FF))
                Spacer(Modifier.height(16.dp))
                Text(progressMessage, color = Color.White)
            }
        } else if (results.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No results found. Try a different query.", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                itemsIndexed(results) { _, item ->
                    ResultCard(item = item, onOpen = { onOpenItem(item) })
                }
            }
        }
    }
}

@Composable
fun ResultCard(item: RankedItem, onOpen: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0xFF0A84FF), androidx.compose.foundation.shape.CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("#${item.rank}", color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.scanResult.name, color = Color.White, fontWeight = FontWeight.SemiBold, maxLines = 2)
                Spacer(Modifier.height(4.dp))
                val metric = item.scanResult.price
                    ?: item.scanResult.viewCount
                    ?: item.scanResult.likeCount
                    ?: ""
                if (metric.isNotBlank()) Text(metric, color = Color(0xFF0A84FF))
            }
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = onOpen,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A84FF))
            ) {
                Text("Open")
            }
        }
    }
}

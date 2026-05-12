package com.scrollbot

import android.content.Context
import android.content.Intent
import com.scrollbot.accessibility.ScrollBotAccessibilityService
import com.scrollbot.capture.ScreenCaptureManager
import com.scrollbot.data.AppTarget
import com.scrollbot.data.RankedItem
import com.scrollbot.monetization.AffiliateManager
import com.scrollbot.ranker.ResultRanker
import com.scrollbot.vision.MoondreamInference
import kotlinx.coroutines.delay

class ScanOrchestrator(
    private val context: Context,
    private val capture: ScreenCaptureManager,
    private val inference: MoondreamInference = MoondreamInference(),
    private val ranker: ResultRanker = ResultRanker(),
    private val affiliate: AffiliateManager = AffiliateManager("scrollbot_aff_001")
) {

    data class ScanProgress(
        val stage: Stage,
        val current: Int = 0,
        val total: Int = 0,
        val message: String = ""
    )

    enum class Stage { OPENING_APP, SCROLLING, ANALYZING, RANKING, DONE, ERROR }

    suspend fun scan(
        query: String,
        target: AppTarget,
        onProgress: (ScanProgress) -> Unit
    ): List<RankedItem> {

        val accessibility = ScrollBotAccessibilityService.instance
            ?: return emptyList<RankedItem>().also {
                onProgress(ScanProgress(Stage.ERROR, message = "Accessibility service not enabled"))
            }

        // 1. Open target app
        onProgress(ScanProgress(Stage.OPENING_APP, message = "Opening ${target.name}..."))
        val launchIntent = context.packageManager.getLaunchIntentForPackage(target.packageName)
            ?: return emptyList<RankedItem>().also {
                onProgress(ScanProgress(Stage.ERROR, message = "${target.name} not installed"))
            }
        context.startActivity(launchIntent.apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
        delay(2000)

        // 2. Type search query
        var searchDone = false
        accessibility.typeInSearchBar(query) { searchDone = true }
        var waited = 0
        while (!searchDone && waited < 5000) { delay(200); waited += 200 }
        delay(1500)

        // 3. Scroll and capture
        val screenshots = mutableListOf<ByteArray>()
        val totalScrolls = 15
        repeat(totalScrolls) { i ->
            onProgress(ScanProgress(Stage.SCROLLING, i + 1, totalScrolls, "Scrolling ${i + 1}/$totalScrolls..."))
            var scrollDone = false
            accessibility.scrollDown { scrollDone = true }
            waited = 0
            while (!scrollDone && waited < 2000) { delay(100); waited += 100 }
            delay(300)
            if (i % 2 == 0) {
                val shot = capture.captureScreenshot()
                if (shot != null) screenshots += shot
            }
        }

        // 4. Vision inference
        val results = inference.analyzeScreenshots(screenshots, target) { done, total ->
            onProgress(ScanProgress(Stage.ANALYZING, done, total, "Analyzing screenshot $done/$total..."))
        }

        // 5. Rank
        onProgress(ScanProgress(Stage.RANKING, message = "Finding best results..."))
        val ranked = ranker.rank(results, target, query)

        // 6. Inject affiliate links for Lazada
        val final = if (target == AppTarget.LAZADA) {
            ranked.map { it.copy(deepLink = it.deepLink?.let { url -> affiliate.injectAffiliate(url) }) }
        } else ranked

        onProgress(ScanProgress(Stage.DONE))
        return final
    }
}

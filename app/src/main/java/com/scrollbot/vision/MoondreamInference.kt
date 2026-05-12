package com.scrollbot.vision

import com.scrollbot.data.AppTarget
import com.scrollbot.data.ScanResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException

class MoondreamInference(private val jni: MoondreamJNI = MoondreamJNI) {

    companion object {
        fun buildPrompt(target: AppTarget): String = when (target) {
            AppTarget.LAZADA ->
                """List every product visible in this screenshot.
For each product return a JSON object with keys: name, price, rating, position_x, position_y.
Return a JSON array of these objects. JSON only, no explanation."""

            AppTarget.YOUTUBE ->
                """List every video visible in this screenshot.
For each video return a JSON object with keys: title, channel, views, position_x, position_y.
Return a JSON array. JSON only."""

            AppTarget.INSTAGRAM ->
                """List every reel visible in this screenshot.
For each reel return a JSON object with keys: caption, likes, timestamp, position_x, position_y.
Return a JSON array. JSON only."""
        }

        fun parseResponse(json: String, target: AppTarget, screenshotIndex: Int): List<ScanResult> {
            return try {
                val trimmed = json.trim().let {
                    val start = it.indexOf('[')
                    val end = it.lastIndexOf(']')
                    if (start != -1 && end != -1) it.substring(start, end + 1) else it
                }
                val array = JSONArray(trimmed)
                (0 until array.length()).mapNotNull { i ->
                    val obj = array.getJSONObject(i)
                    when (target) {
                        AppTarget.LAZADA -> ScanResult(
                            name = obj.optString("name").ifBlank { return@mapNotNull null },
                            price = obj.optString("price").ifBlank { null },
                            rating = obj.optString("rating").toFloatOrNull(),
                            positionX = obj.optInt("position_x"),
                            positionY = obj.optInt("position_y"),
                            screenshotIndex = screenshotIndex,
                            appTarget = target
                        )
                        AppTarget.YOUTUBE -> ScanResult(
                            name = obj.optString("title").ifBlank { return@mapNotNull null },
                            channelName = obj.optString("channel").ifBlank { null },
                            viewCount = obj.optString("views").ifBlank { null },
                            positionX = obj.optInt("position_x"),
                            positionY = obj.optInt("position_y"),
                            screenshotIndex = screenshotIndex,
                            appTarget = target
                        )
                        AppTarget.INSTAGRAM -> ScanResult(
                            name = "reel_$screenshotIndex",
                            caption = obj.optString("caption").ifBlank { null },
                            likeCount = obj.optString("likes").ifBlank { null },
                            timestamp = obj.optString("timestamp").ifBlank { null },
                            positionX = obj.optInt("position_x"),
                            positionY = obj.optInt("position_y"),
                            screenshotIndex = screenshotIndex,
                            appTarget = target
                        )
                    }
                }
            } catch (e: JSONException) {
                emptyList()
            }
        }
    }

    suspend fun analyzeScreenshots(
        screenshots: List<ByteArray>,
        target: AppTarget,
        onProgress: (Int, Int) -> Unit
    ): List<ScanResult> = withContext(Dispatchers.Default) {
        val prompt = buildPrompt(target)
        val allResults = mutableListOf<ScanResult>()
        screenshots.forEachIndexed { index, bytes ->
            onProgress(index + 1, screenshots.size)
            val response = MoondreamJNI.queryImage(bytes, prompt)
            allResults += parseResponse(response, target, screenshotIndex = index)
        }
        allResults
    }
}

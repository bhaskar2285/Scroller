package com.scrollbot.ranker

import com.scrollbot.data.AppTarget
import com.scrollbot.data.RankedItem
import com.scrollbot.data.ScanResult
import kotlin.math.ln

class ResultRanker {

    fun rank(items: List<ScanResult>, target: AppTarget, query: String): List<RankedItem> {
        val scored = items.map { item ->
            val score = when (target) {
                AppTarget.LAZADA -> scoreLazada(item)
                AppTarget.YOUTUBE -> scoreYoutube(item, query)
                AppTarget.INSTAGRAM -> scoreInstagram(item, query)
            }
            Pair(item, score)
        }
        return scored
            .sortedByDescending { it.second }
            .take(5)
            .mapIndexed { index, (item, score) ->
                RankedItem(rank = index + 1, scanResult = item, score = score)
            }
    }

    private fun scoreLazada(item: ScanResult): Float {
        val rating = item.rating ?: 3.0f
        val price = parsePrice(item.price) ?: 100f
        val normalizedPrice = (200f - price.coerceIn(0f, 200f)) / 200f
        return rating * 0.6f + normalizedPrice * 0.4f
    }

    private fun scoreYoutube(item: ScanResult, query: String): Float {
        val views = parseViewCount(item.viewCount) ?: 0L
        val viewScore = if (views > 0) (ln(views.toDouble()) / ln(10_000_000.0)).toFloat() else 0f
        val relevance = queryRelevance(item.name, query)
        return relevance * 0.6f + viewScore * 0.4f
    }

    private fun scoreInstagram(item: ScanResult, query: String): Float {
        val likes = item.likeCount?.replace(",", "")?.toLongOrNull() ?: 0L
        val likeScore = if (likes > 0) (ln(likes.toDouble()) / ln(1_000_000.0)).toFloat().coerceIn(0f, 1f) else 0f
        val recency = parseRecencyScore(item.timestamp)
        val relevance = queryRelevance(item.caption ?: "", query)
        return likeScore * 0.5f + recency * 0.3f + relevance * 0.2f
    }

    private fun parsePrice(price: String?): Float? {
        if (price == null) return null
        return Regex("[0-9]+\\.?[0-9]*").find(price)?.value?.toFloatOrNull()
    }

    private fun parseViewCount(views: String?): Long? {
        if (views == null) return null
        val cleaned = views.lowercase().replace(",", "").replace(" views", "").trim()
        return when {
            cleaned.endsWith("m") -> (cleaned.dropLast(1).toFloatOrNull()?.times(1_000_000))?.toLong()
            cleaned.endsWith("k") -> (cleaned.dropLast(1).toFloatOrNull()?.times(1_000))?.toLong()
            else -> cleaned.toLongOrNull()
        }
    }

    private fun parseRecencyScore(timestamp: String?): Float {
        if (timestamp == null) return 0f
        val lower = timestamp.lowercase()
        return when {
            lower.contains("h") -> 1.0f
            lower.contains("1d") || lower.contains("2d") -> 0.8f
            lower.contains("3d") || lower.contains("4d") || lower.contains("5d") -> 0.6f
            lower.contains("w") -> 0.3f
            else -> 0.1f
        }
    }

    private fun queryRelevance(text: String, query: String): Float {
        val words = query.lowercase().split(" ")
        val textLower = text.lowercase()
        val matches = words.count { textLower.contains(it) }
        return if (words.isEmpty()) 0f else matches.toFloat() / words.size
    }
}

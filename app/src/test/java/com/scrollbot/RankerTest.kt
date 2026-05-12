package com.scrollbot

import com.scrollbot.data.AppTarget
import com.scrollbot.data.ScanResult
import com.scrollbot.ranker.ResultRanker
import org.junit.Assert.*
import org.junit.Test

class RankerTest {

    private val ranker = ResultRanker()

    @Test
    fun `Lazada ranks higher rating over lower price when both differ`() {
        val cheap = ScanResult("Cheap", price = "RM 10.00", rating = 2.0f, appTarget = AppTarget.LAZADA)
        val quality = ScanResult("Quality", price = "RM 30.00", rating = 4.8f, appTarget = AppTarget.LAZADA)
        val ranked = ranker.rank(listOf(cheap, quality), AppTarget.LAZADA, "shoes")
        assertEquals("Quality", ranked[0].scanResult.name)
    }

    @Test
    fun `YouTube ranks by view count when titles equally relevant`() {
        val lowViews = ScanResult("Python Tutorial", viewCount = "1K views", appTarget = AppTarget.YOUTUBE)
        val highViews = ScanResult("Python Tutorial", viewCount = "5M views", appTarget = AppTarget.YOUTUBE)
        val ranked = ranker.rank(listOf(lowViews, highViews), AppTarget.YOUTUBE, "python")
        assertEquals("5M views", ranked[0].scanResult.viewCount)
    }

    @Test
    fun `Instagram ranks by likes`() {
        val few = ScanResult("reel", likeCount = "100", caption = "motivation mindset", appTarget = AppTarget.INSTAGRAM)
        val many = ScanResult("reel2", likeCount = "50000", caption = "motivation mindset", appTarget = AppTarget.INSTAGRAM)
        val ranked = ranker.rank(listOf(few, many), AppTarget.INSTAGRAM, "motivation")
        assertEquals("50000", ranked[0].scanResult.likeCount)
    }

    @Test
    fun `rank returns top 5 max`() {
        val items = (1..10).map {
            ScanResult("Product $it", price = "RM ${it * 10}.00", rating = it * 0.5f, appTarget = AppTarget.LAZADA)
        }
        val ranked = ranker.rank(items, AppTarget.LAZADA, "shoes")
        assertTrue(ranked.size <= 5)
    }

    @Test
    fun `rank assigns sequential rank numbers starting at 1`() {
        val items = listOf(
            ScanResult("A", price = "RM 20.00", rating = 4.0f, appTarget = AppTarget.LAZADA),
            ScanResult("B", price = "RM 15.00", rating = 3.5f, appTarget = AppTarget.LAZADA)
        )
        val ranked = ranker.rank(items, AppTarget.LAZADA, "bag")
        assertEquals(1, ranked[0].rank)
        assertEquals(2, ranked[1].rank)
    }
}

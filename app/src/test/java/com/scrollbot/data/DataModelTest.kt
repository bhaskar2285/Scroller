package com.scrollbot.data

import org.junit.Assert.*
import org.junit.Test

class DataModelTest {

    @Test
    fun `ScanResult defaults are null for optional fields`() {
        val result = ScanResult(
            name = "Test Product",
            appTarget = AppTarget.LAZADA
        )
        assertNull(result.price)
        assertNull(result.rating)
        assertEquals(AppTarget.LAZADA, result.appTarget)
    }

    @Test
    fun `AppTarget has correct package names`() {
        assertEquals("com.lazada.android", AppTarget.LAZADA.packageName)
        assertEquals("com.google.android.youtube", AppTarget.YOUTUBE.packageName)
        assertEquals("com.instagram.android", AppTarget.INSTAGRAM.packageName)
    }

    @Test
    fun `RankedItem holds rank and score`() {
        val item = RankedItem(
            rank = 1,
            scanResult = ScanResult(name = "Item", appTarget = AppTarget.LAZADA),
            score = 0.95f
        )
        assertEquals(1, item.rank)
        assertEquals(0.95f, item.score)
    }
}

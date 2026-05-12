package com.scrollbot

import com.scrollbot.monetization.AffiliateManager
import org.junit.Assert.*
import org.junit.Test

class AffiliateManagerTest {

    private val manager = AffiliateManager(affiliateId = "scrollbot_aff_001")

    @Test
    fun `injects affiliate param into Lazada URL`() {
        val url = "https://www.lazada.com/products/nike-shoes-i12345-s67890.html"
        val result = manager.injectAffiliate(url)
        assertTrue(result.contains("af_sub1=scrollbot_aff_001"))
    }

    @Test
    fun `does not modify YouTube URLs`() {
        val url = "https://youtube.com/watch?v=abc123"
        val result = manager.injectAffiliate(url)
        assertEquals(url, result)
    }

    @Test
    fun `does not modify Instagram URLs`() {
        val url = "https://instagram.com/reel/abc123"
        val result = manager.injectAffiliate(url)
        assertEquals(url, result)
    }

    @Test
    fun `handles Lazada URL that already has query params`() {
        val url = "https://www.lazada.com/products/item.html?spm=abc"
        val result = manager.injectAffiliate(url)
        assertTrue(result.contains("af_sub1=scrollbot_aff_001"))
        assertTrue(result.contains("spm=abc"))
    }

    @Test
    fun `returns original URL if null or blank`() {
        assertEquals("", manager.injectAffiliate(""))
    }
}

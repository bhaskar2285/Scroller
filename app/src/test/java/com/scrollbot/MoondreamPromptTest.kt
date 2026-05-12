package com.scrollbot

import com.scrollbot.data.AppTarget
import com.scrollbot.vision.MoondreamInference
import org.junit.Assert.*
import org.junit.Test

class MoondreamPromptTest {

    @Test
    fun `Lazada prompt asks for name price rating`() {
        val prompt = MoondreamInference.buildPrompt(AppTarget.LAZADA)
        assertTrue(prompt.contains("price"))
        assertTrue(prompt.contains("rating"))
        assertTrue(prompt.contains("JSON"))
    }

    @Test
    fun `YouTube prompt asks for title views channel`() {
        val prompt = MoondreamInference.buildPrompt(AppTarget.YOUTUBE)
        assertTrue(prompt.contains("title"))
        assertTrue(prompt.contains("views"))
        assertTrue(prompt.contains("JSON"))
    }

    @Test
    fun `Instagram prompt asks for caption likes timestamp`() {
        val prompt = MoondreamInference.buildPrompt(AppTarget.INSTAGRAM)
        assertTrue(prompt.contains("caption"))
        assertTrue(prompt.contains("likes"))
        assertTrue(prompt.contains("JSON"))
    }

    @Test
    fun `parseResponse extracts list of items from JSON array`() {
        val json = """[{"name":"Nike Shoes","price":"RM 89.00","rating":"4.5","position_x":"100","position_y":"300"}]"""
        val results = MoondreamInference.parseResponse(json, AppTarget.LAZADA, screenshotIndex = 0)
        assertEquals(1, results.size)
        assertEquals("Nike Shoes", results[0].name)
        assertEquals("RM 89.00", results[0].price)
        assertEquals(4.5f, results[0].rating)
        assertEquals(100, results[0].positionX)
    }

    @Test
    fun `parseResponse handles malformed JSON gracefully`() {
        val results = MoondreamInference.parseResponse("not json", AppTarget.LAZADA, screenshotIndex = 0)
        assertEquals(emptyList<Any>(), results)
    }
}

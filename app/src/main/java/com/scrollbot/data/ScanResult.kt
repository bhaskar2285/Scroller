package com.scrollbot.data

data class ScanResult(
    val name: String,
    val price: String? = null,        // Lazada: "RM 49.90"
    val rating: Float? = null,        // Lazada: 4.5
    val viewCount: String? = null,    // YouTube: "1.2M views"
    val channelName: String? = null,  // YouTube
    val likeCount: String? = null,    // Instagram
    val timestamp: String? = null,    // Instagram: "2d ago"
    val caption: String? = null,      // Instagram
    val screenshotIndex: Int = 0,
    val positionX: Int = 0,
    val positionY: Int = 0,
    val appTarget: AppTarget
)

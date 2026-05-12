package com.scrollbot.data

data class RankedItem(
    val rank: Int,
    val scanResult: ScanResult,
    val score: Float,
    val deepLink: String? = null,
    val thumbnailPath: String? = null  // path to cropped screenshot region
)

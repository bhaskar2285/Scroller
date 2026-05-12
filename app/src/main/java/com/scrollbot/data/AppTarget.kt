package com.scrollbot.data

enum class AppTarget(
    val packageName: String,
    val searchActivityPattern: String
) {
    LAZADA("com.lazada.android", "com.lazada.android.ui.search"),
    YOUTUBE("com.google.android.youtube", "com.google.android.youtube.app.honeycomb.Shell"),
    INSTAGRAM("com.instagram.android", "com.instagram.android.activity.MainTabActivity")
}

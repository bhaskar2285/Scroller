package com.scrollbot.monetization

import android.content.Context
import android.content.SharedPreferences

class SubscriptionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("scrollbot_prefs", Context.MODE_PRIVATE)

    companion object {
        const val FREE_DAILY_LIMIT = 5
        const val PREF_SCAN_COUNT = "scan_count"
        const val PREF_LAST_SCAN_DATE = "last_scan_date"
        const val PREF_IS_PRO = "is_pro"
    }

    val isPro: Boolean get() = prefs.getBoolean(PREF_IS_PRO, false)

    fun canScan(): Boolean {
        if (isPro) return true
        resetCountIfNewDay()
        return prefs.getInt(PREF_SCAN_COUNT, 0) < FREE_DAILY_LIMIT
    }

    fun scansRemainingToday(): Int {
        if (isPro) return Int.MAX_VALUE
        resetCountIfNewDay()
        return (FREE_DAILY_LIMIT - prefs.getInt(PREF_SCAN_COUNT, 0)).coerceAtLeast(0)
    }

    fun recordScan() {
        resetCountIfNewDay()
        prefs.edit().putInt(PREF_SCAN_COUNT, prefs.getInt(PREF_SCAN_COUNT, 0) + 1).apply()
    }

    fun setPro(value: Boolean) {
        prefs.edit().putBoolean(PREF_IS_PRO, value).apply()
    }

    private fun resetCountIfNewDay() {
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            .format(java.util.Date())
        val lastDate = prefs.getString(PREF_LAST_SCAN_DATE, "")
        if (today != lastDate) {
            prefs.edit()
                .putInt(PREF_SCAN_COUNT, 0)
                .putString(PREF_LAST_SCAN_DATE, today)
                .apply()
        }
    }
}

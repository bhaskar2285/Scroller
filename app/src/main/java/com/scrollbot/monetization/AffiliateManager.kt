package com.scrollbot.monetization

import android.net.Uri

class AffiliateManager(private val affiliateId: String) {

    fun injectAffiliate(url: String): String {
        if (url.isBlank()) return url
        if (!url.contains("lazada.com")) return url

        return try {
            val uri = Uri.parse(url)
            uri.buildUpon()
                .appendQueryParameter("af_sub1", affiliateId)
                .build()
                .toString()
        } catch (e: Exception) {
            url
        }
    }
}

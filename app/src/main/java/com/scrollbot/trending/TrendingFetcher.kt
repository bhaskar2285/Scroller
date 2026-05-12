package com.scrollbot.trending

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.net.URL

class TrendingFetcher {

    suspend fun fetchTrending(countryCode: String = "MY"): List<String> =
        withContext(Dispatchers.IO) {
            try {
                val url = URL("https://trends.google.com/trending/rss?geo=$countryCode")
                val connection = url.openConnection().apply { connectTimeout = 5000; readTimeout = 5000 }
                val stream = connection.getInputStream()

                val factory = XmlPullParserFactory.newInstance()
                val parser = factory.newPullParser()
                parser.setInput(stream, "UTF-8")

                val titles = mutableListOf<String>()
                var eventType = parser.eventType
                var inTitle = false

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    when (eventType) {
                        XmlPullParser.START_TAG -> if (parser.name == "title") inTitle = true
                        XmlPullParser.TEXT -> if (inTitle) {
                            val text = parser.text.trim()
                            if (text.isNotBlank() && !text.startsWith("Google Trends")) {
                                titles += text
                            }
                            inTitle = false
                        }
                        XmlPullParser.END_TAG -> inTitle = false
                    }
                    eventType = parser.next()
                }
                stream.close()
                titles.take(10)
            } catch (e: Exception) {
                emptyList()
            }
        }
}

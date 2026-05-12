package com.scrollbot.onboarding

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

class ModelDownloadManager(private val context: Context) {

    companion object {
        // moondream2 f16 from HuggingFace (~3.5 GB total for both files)
        const val MODEL_URL = "https://huggingface.co/vikhyatk/moondream2/resolve/main/moondream2-text-model-f16.gguf"
        const val MMPROJ_URL = "https://huggingface.co/vikhyatk/moondream2/resolve/main/moondream2-mmproj-f16.gguf"
        const val MODEL_FILE = "moondream2.gguf"
        const val MMPROJ_FILE = "moondream2-mmproj.gguf"
    }

    val modelFile: File get() = File(context.filesDir, "models/$MODEL_FILE")
    val mmprojFile: File get() = File(context.filesDir, "models/$MMPROJ_FILE")
    val isModelReady: Boolean get() = modelFile.exists() && mmprojFile.exists()

    private val client = OkHttpClient()

    suspend fun downloadAll(onProgress: (Float, String) -> Unit) {
        File(context.filesDir, "models").mkdirs()
        onProgress(0f, "Downloading vision model...")
        downloadFile(MODEL_URL, modelFile) { progress ->
            onProgress(progress * 0.7f, "Downloading vision model... ${(progress * 100).toInt()}%")
        }
        onProgress(0.7f, "Downloading vision projector...")
        downloadFile(MMPROJ_URL, mmprojFile) { progress ->
            onProgress(0.7f + progress * 0.3f, "Downloading projector... ${(progress * 100).toInt()}%")
        }
        onProgress(1f, "Ready!")
    }

    private suspend fun downloadFile(url: String, dest: File, onProgress: (Float) -> Unit) =
        withContext(Dispatchers.IO) {
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val body = response.body ?: error("Empty response for $url")
            val total = body.contentLength().takeIf { it > 0 } ?: 1L

            FileOutputStream(dest).use { out ->
                body.byteStream().use { input ->
                    val buffer = ByteArray(8192)
                    var downloaded = 0L
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        out.write(buffer, 0, read)
                        downloaded += read
                        onProgress(downloaded.toFloat() / total)
                    }
                }
            }
        }
}

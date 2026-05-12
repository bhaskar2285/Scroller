package com.scrollbot.vision

object MoondreamJNI {
    init {
        System.loadLibrary("moondream_jni")
    }

    external fun loadModel(modelPath: String, mmprojPath: String): Boolean
    external fun queryImage(imageBytes: ByteArray, prompt: String): String
    external fun freeModel()
}

package com.scrollbot.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Path
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ScrollBotAccessibilityService : AccessibilityService() {

    companion object {
        var instance: ScrollBotAccessibilityService? = null
        const val ACTION_TYPE_SEARCH = "com.scrollbot.TYPE_SEARCH"
        const val EXTRA_QUERY = "query"
    }

    private val scope = CoroutineScope(Dispatchers.Main)

    override fun onServiceConnected() {
        instance = this
    }

    override fun onDestroy() {
        instance = null
        super.onDestroy()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}

    fun typeInSearchBar(query: String, onDone: () -> Unit) {
        scope.launch {
            delay(1500) // wait for app to load
            val root = rootInActiveWindow ?: return@launch
            val searchNode = findSearchNode(root)
            if (searchNode != null) {
                searchNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                delay(500)
                val args = Bundle()
                args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, query)
                searchNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
                delay(300)
                // simulate enter/search
                searchNode.performAction(AccessibilityNodeInfo.ACTION_IME_ENTER)
                delay(1000)
                onDone()
            }
            root.recycle()
        }
    }

    fun scrollDown(onDone: () -> Unit) {
        val displayMetrics = resources.displayMetrics
        val screenHeight = displayMetrics.heightPixels
        val screenWidth = displayMetrics.widthPixels

        val path = Path().apply {
            moveTo(screenWidth / 2f, screenHeight * 0.7f)
            lineTo(screenWidth / 2f, screenHeight * 0.3f)
        }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 400))
            .build()

        dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription) {
                onDone()
            }
        }, null)
    }

    fun tapAt(x: Int, y: Int, onDone: () -> Unit) {
        val path = Path().apply { moveTo(x.toFloat(), y.toFloat()) }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 50))
            .build()
        dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription) { onDone() }
        }, null)
    }

    fun pressBack() {
        performGlobalAction(GLOBAL_ACTION_BACK)
    }

    private fun findSearchNode(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        // Try common search field resource IDs
        val searchIds = listOf(
            "com.lazada.android:id/search_bar_input",
            "com.google.android.youtube:id/search_edit_text",
            "com.instagram.android:id/action_bar_search_edit_text"
        )
        for (id in searchIds) {
            val nodes = root.findAccessibilityNodeInfosByViewId(id)
            if (nodes.isNotEmpty()) return nodes[0]
        }
        // Fallback: find by class name EditText
        return findByClass(root, "android.widget.EditText")
    }

    private fun findByClass(node: AccessibilityNodeInfo, className: String): AccessibilityNodeInfo? {
        if (node.className?.contains(className) == true) return node
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val result = findByClass(child, className)
            if (result != null) return result
            child.recycle()
        }
        return null
    }
}

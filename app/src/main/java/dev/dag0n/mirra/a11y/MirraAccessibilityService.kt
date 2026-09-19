package dev.dag0n.mirra.a11y

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import dev.dag0n.mirra.mirror.MirrorService

class MirraAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        sendBroadcast(Intent(MirrorService.ACTION_A11Y_CHANGED).setPackage(packageName))
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit

    override fun onInterrupt() = Unit

    override fun onUnbind(intent: Intent?): Boolean {
        instance = null
        sendBroadcast(Intent(MirrorService.ACTION_A11Y_CHANGED).setPackage(packageName))
        return super.onUnbind(intent)
    }

    fun inject(gesture: GestureDescription, done: (Boolean) -> Unit): Boolean {
        return dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) = done(true)
            override fun onCancelled(gestureDescription: GestureDescription?) = done(false)
        }, null)
    }

    companion object {
        @Volatile
        var instance: MirraAccessibilityService? = null
            private set

        fun isEnabled(): Boolean = instance != null
    }
}

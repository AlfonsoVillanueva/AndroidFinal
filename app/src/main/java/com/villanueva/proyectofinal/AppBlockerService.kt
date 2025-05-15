package com.villanueva.proyectofinal

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.content.Intent

class AppBlockerService : AccessibilityService() {

    private val blockedApps = listOf("com.google.android.youtube")


    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString()
            if (packageName != null && blockedApps.contains(packageName)) {
                val intent = Intent(this, BlockedAppActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
    }

    override fun onInterrupt() {}
}

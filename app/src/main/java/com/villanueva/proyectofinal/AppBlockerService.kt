package com.villanueva.proyectofinal

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.content.Intent

class AppBlockerService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return


        val currentApp = event.packageName?.toString() ?: return            //App actual
        val blockedApps = SelectedAppsManager.getSelectedApps(this) //Apps bloqueadas

        //Comprobar y bloquear
        if (blockedApps.contains(currentApp)) {
            // Abrimos la Activity que bloquea
            val intent = Intent(this, BlockedAppActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    override fun onInterrupt() {
        // Aquí puedes manejar interrupciones del servicio si lo deseas
    }
}

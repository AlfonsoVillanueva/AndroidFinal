package com.villanueva.proyectofinal

import android.accessibilityservice.AccessibilityService
import android.content.ContentValues.TAG
import android.view.accessibility.AccessibilityEvent
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast

class AppBlockerService : AccessibilityService() {
    private var lastPackageName: String? = null
    private val handler = Handler(Looper.getMainLooper())
    private val checkRunnable = object : Runnable {
        override fun run() {
            checkCurrentApp()
            handler.postDelayed(this, 1000) // Revisa cada x segundos (ajustable)
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        handler.post(checkRunnable)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(checkRunnable)
    }

    private fun checkCurrentApp() {
        val currentApp = getForegroundAppPackageName() ?: return
        val blockedAppsData = SelectedAppsManager.getSelectedAppDataList(this)
        val appData = blockedAppsData.find { it.packageName == currentApp }

        if (appData != null) {
            val currentTime = System.currentTimeMillis()

            Toast.makeText(this, "Verificando: $currentApp", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "Verificando app activa: $currentApp")

            if (currentTime >= appData.nextChangeTime) {

                Toast.makeText(this, "Tiempo alcanzado, actualizando estado", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Tiempo alcanzado, actualizando estado")
                appData.updateState()
                val updatedList = blockedAppsData.map {
                    if (it.packageName == currentApp) appData else it
                }
                SelectedAppsManager.saveAppDataList(this, updatedList)
            }

            if (appData.isBlocked) {
                val remainingTime = appData.nextChangeTime - System.currentTimeMillis()

                val intent = Intent(this, BlockedAppActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intent.putExtra("remainingTimeMillis", remainingTime)
                startActivity(intent)
            }

        }
    }


    private fun getForegroundAppPackageName(): String? {
        val usm = getSystemService(USAGE_STATS_SERVICE) as android.app.usage.UsageStatsManager
        val time = System.currentTimeMillis()
        //Estadisticas de uso de la app en los ultimos 10 segundos
        val appList = usm.queryUsageStats(
            android.app.usage.UsageStatsManager.INTERVAL_DAILY,
            time - 1000 * 10,
            time
        )

        /*
        if (appList.isNullOrEmpty()) {
            Toast.makeText(this, "La lista de apps del UsageStatsManager está vacía. ¿Tienes el permiso USA", Toast.LENGTH_SHORT).show()
            Log.w(TAG, "La lista de apps del UsageStatsManager está vacía. ¿Tienes el permiso USAGE_ACCESS?")
            return null
        }
        */

        val sortedList = appList.sortedByDescending { it.lastTimeUsed }
        return sortedList.firstOrNull()?.packageName
    }

    override fun onInterrupt() {
        // Aquí puedes manejar interrupciones del servicio si lo deseas
    }

}

package com.villanueva.proyectofinal

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast

class AppBlockerService : AccessibilityService() {
    private var blockedActivityVisible = false
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
        val blockedAppsData = SelectedAppsManager.getSelectedAppDataList(this).toMutableList()
        val currentTime = System.currentTimeMillis()
        var updated = false

        val currentAppData = blockedAppsData.find { it.packageName == currentApp }

        if (currentAppData == null) {
            Log.d("AppBlockerService", "La app actual ($currentApp) no está en la lista de apps bloqueadas.")
            return
        }else{
            for(appData in blockedAppsData){
                //Desbloquear otras applicacion que no son la actual si ya se termino el tiempo
                if (appData.isBlocked && currentTime >= appData.unblockAtTimestamp) {
                    appData.isBlocked = false
                    appData.usageTimeAccumulated = 0L
                    appData.lastForegroundTimestamp = 0L
                    appData.unblockAtTimestamp = 0L
                    updated = true
                    Log.d("AppBlockerService", "${appData.packageName} ha sido desbloqueada después del tiempo de bloqueo.")
                }

                if(appData.packageName == currentApp){
                    if (!appData.isBlocked) {
                        if (appData.lastForegroundTimestamp == 0L) {
                            // Primer registro de tiempo de primer plano
                            appData.lastForegroundTimestamp = currentTime
                            updated = true
                        } else {
                            val delta = currentTime - appData.lastForegroundTimestamp
                            if (delta > 0) {
                                appData.usageTimeAccumulated += delta
                                appData.lastForegroundTimestamp = currentTime
                                updated = true
                                Log.d("AppBlockerService", "Tiempo acumulado para ${appData.packageName}: ${appData.usageTimeAccumulated} ms")
                            }

                        }
                        //Verificar el tiempo de uso y comprobar si el tiempo de uso es igual al tiempo de bloqueo
                        if (appData.usageTimeAccumulated >= BlockedAppData.usageLimit) {
                            appData.isBlocked = true
                            appData.unblockAtTimestamp = currentTime + BlockedAppData.blockDuration
                            updated = true
                            Log.d("AppBlockerService", "${appData.packageName} alcanzó el límite de uso y ha sido bloqueada.")

                            //Mandarllamar a la activite de bloqueo
                            if (appData.isBlocked && appData.packageName == currentApp) {
                                // Verificar si la actividad no está ya lanzada para evitar múltiples intentos
                                val shouldShowBlockScreen = !isBlockedActivityVisible() || !isBlockedAppInForeground(currentApp)

                                if (shouldShowBlockScreen) {
                                    val intent = Intent(this, BlockedAppActivity::class.java)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                    intent.putExtra("remainingTimeMillis", appData.unblockAtTimestamp - currentTime)
                                    startActivity(intent)
                                    setBlockedActivityVisible(true)
                                }
                            }
                        }
                    }else{
                        if (currentTime >= appData.unblockAtTimestamp) {
                            // Ya se cumplió el tiempo de bloqueo para la app actual
                            appData.isBlocked = false
                            appData.usageTimeAccumulated = 0L
                            appData.lastForegroundTimestamp = 0L
                            appData.unblockAtTimestamp = 0L
                            updated = true
                            Log.d("AppBlockerService", "${appData.packageName} ha sido desbloqueada después del tiempo de bloqueo.")
                        } else {
                            // Mostrar pantalla de bloqueo si aún no está visible
                            val shouldShowBlockScreen = !isBlockedActivityVisible() || !isBlockedAppInForeground(currentApp)

                            if (shouldShowBlockScreen) {
                                val intent = Intent(this, BlockedAppActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                intent.putExtra("remainingTimeMillis", appData.unblockAtTimestamp - currentTime)
                                startActivity(intent)
                                setBlockedActivityVisible(true)
                            }
                        }
                    }
                }else{
                    // Reiniciar el timestamp porque l= currentAa app está en segundo plano
                    if (appData.lastForegroundTimestamp != 0L) {
                        appData.lastForegroundTimestamp = 0L
                        updated = true
                    }
                }
            }

            // Después de actualizar todos los estados y tiempos
            if (updated) {
                SelectedAppsManager.saveAppDataList(this, blockedAppsData)
                Log.d("AppBlockerService", "Lista de apps bloqueadas actualizada guardada.")
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
            Log.w(TAG, "La lista de apps del UsageStatsManager está vacía. ¿Tienes el permiso USAGE_ACCESS?")
            return null
        }
        */

        val sortedList = appList.sortedByDescending { it.lastTimeUsed }
        return sortedList.firstOrNull()?.packageName
    }

    private fun isBlockedAppInForeground(currentApp: String): Boolean {
        // Aquí puedes comparar si la app actual es la bloqueada, pero NO la actividad de bloqueo.
        return currentApp == getForegroundAppPackageName()
    }
    override fun onInterrupt() {
        // Aquí puedes manejar interrupciones del servicio si lo deseas
    }

    companion object {
        private var blockedActivityVisible = false

        fun isBlockedActivityVisible(): Boolean {
            return blockedActivityVisible
        }

        fun setBlockedActivityVisible(visible: Boolean) {
            blockedActivityVisible = visible
        }



    }
}

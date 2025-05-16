package com.villanueva.proyectofinal

import android.app.usage.UsageStatsManager
import android.app.usage.UsageEvents
import android.os.Handler
import android.os.Looper
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import android.accessibilityservice.AccessibilityService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.util.Log
import androidx.core.app.NotificationCompat

class AppBlockerService : AccessibilityService() {
    private val usoApps: MutableMap<String, AppUsageInfo> = mutableMapOf()

    private val handler = Handler(Looper.getMainLooper())
    private val checkUsageRunnable = object : Runnable {
        override fun run() {
            checkUsageAndBlock()
            handler.postDelayed(this, 1000) // Revisa cada 1 segundo
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        crearCanalNotificaciones()
        handler.post(checkUsageRunnable) // Inicia la verificación periódica
        Log.d("AppBlockerService", "Servicio conectado y verificación iniciada")
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(checkUsageRunnable) // Detiene la verificación
        Log.d("AppBlockerService", "Servicio destruido y verificación detenida")
    }

    private fun mostrarPantallaBloqueo(paquete: String) {
        val intent = Intent(applicationContext, BlockedAppActivity::class.java).apply {
            putExtra("paqueteBloqueado", paquete)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        Log.d("AppBlockerService", "Lanzando pantalla de bloqueo para $paquete")
        startActivity(intent)
    }
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val paqueteActual = event.packageName?.toString() ?: return
            val ahora = System.currentTimeMillis()

            val selectedApps = SelectedAppsManager.getSelectedApps(this)
            if (!selectedApps.contains(paqueteActual)) return

            // Verifica si ya está en periodo de bloqueo
            if (AppUsageTracker.isInBlockPeriod(this, paqueteActual)) {
                mostrarPantallaBloqueo(paqueteActual)
                return
            }

            // Gestiona la acumulación del tiempo de uso
            val info = usoApps.getOrPut(paqueteActual) { AppUsageInfo() }

            // Si ya estaba visible, acumula el tiempo de uso
            if (info.tiempoUltimaVezVisible != 0L) {
                val tiempoVisible = ahora - info.tiempoUltimaVezVisible
                info.tiempoUsoEnMilis += tiempoVisible
                Log.d("AppBlockerService", "Acumulando tiempo para $paqueteActual: +$tiempoVisible ms (total: ${info.tiempoUsoEnMilis} ms)")
            }

            // Actualiza la última vez visible
            info.tiempoUltimaVezVisible = ahora

            // Verifica si ha excedido el tiempo permitido
            val tiempoUsoLimite = Config.MAX_USAGE_TIME_MILLIS
            if (info.tiempoUsoEnMilis >= tiempoUsoLimite) {
                info.tiempoUsoEnMilis = 0L
                info.bloqueadaHasta = ahora + Config.BLOCK_DURATION_MILLIS
                AppUsageTracker.startBlockPeriod(this, paqueteActual)
                mostrarPantallaBloqueo(paqueteActual)
                Log.d("AppBlockerService", "Tiempo límite excedido para $paqueteActual, bloqueo activado")
            }
        }
    }

    private fun checkUsageAndBlock() {
        val selectedApps = SelectedAppsManager.getSelectedApps(this)
        val ahora = System.currentTimeMillis()

        for ((paquete, info) in usoApps) {
            if (!selectedApps.contains(paquete)) continue

            // Calcula el tiempo de uso desde la última vez visible
            if (info.tiempoUltimaVezVisible != 0L) {
                val tiempoDesdeUltima = ahora - info.tiempoUltimaVezVisible
                info.tiempoUsoEnMilis += tiempoDesdeUltima
                info.tiempoUltimaVezVisible = ahora
                Log.d("AppBlockerService", "Chequeo periódico: acumulando $tiempoDesdeUltima ms para $paquete (total: ${info.tiempoUsoEnMilis} ms)")
            }

            val tiempoUsoLimite = Config.USO_PERMITIDO_MINUTOS * 60 * 1000

            if (info.tiempoUsoEnMilis >= tiempoUsoLimite && !AppUsageTracker.isInBlockPeriod(this, paquete)) {
                info.tiempoUsoEnMilis = 0L
                info.bloqueadaHasta = ahora + Config.TIEMPO_BLOQUEO_MINUTOS * 60 * 1000
                AppUsageTracker.startBlockPeriod(this, paquete)
                mostrarPantallaBloqueo(paquete)
                Log.d("AppBlockerService", "Chequeo periódico: bloqueo activado para $paquete")
            }

            if (AppUsageTracker.isInBlockPeriod(this, paquete)) {
                val bloqueadaHasta = info.bloqueadaHasta
                if (bloqueadaHasta != 0L && System.currentTimeMillis() >= bloqueadaHasta) {
                    AppUsageTracker.endBlockPeriod(this, paquete)
                    info.bloqueadaHasta = 0L
                    enviarNotificacionDesbloqueo(paquete)
                    Log.d("AppBlockerService", "Periodo de bloqueo finalizado para $paquete")
                }
            }
        }
    }

    private fun crearCanalNotificaciones() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                "bloqueo_app_channel",
                "Notificaciones de bloqueo",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifica cuando una app excede el tiempo de uso permitido"
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(canal)
        }
    }

    private fun enviarNotificacionDesbloqueo(paquete: String) {
        val builder = NotificationCompat.Builder(this, "bloqueo_app_channel")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("App desbloqueada")
            .setContentText("Ya puedes volver a usar: $paquete")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify((paquete + "_unblock").hashCode(), builder.build())
    }


    override fun onInterrupt() {
        // Implementar si es necesario
    }
}

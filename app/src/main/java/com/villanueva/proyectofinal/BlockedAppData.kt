package com.villanueva.proyectofinal

import java.util.concurrent.TimeUnit

data class BlockedAppData(
    val packageName: String,
    var isBlocked: Boolean = false,

    // Tiempo total de uso acumulado en ms
    var usageTimeAccumulated: Long = 0L,
    // Marca de tiempo del último momento en que la app estuvo activa
    var lastForegroundTimestamp: Long = 0L,
    // Marca cuándo termina el bloqueo
    var unblockAtTimestamp: Long = 0L,

    // Nueva variable: momento en que cambió el estado de bloqueo
    var lastBlockStatusChangeTimestamp: Long = 0L
) {
    fun updateBlockState(currentTime: Long, usageLimit: Long, blockDuration: Long) {
        if (!isBlocked && usageTimeAccumulated >= usageLimit) {
            isBlocked = true
            unblockAtTimestamp = currentTime + blockDuration
        } else if (isBlocked && currentTime >= unblockAtTimestamp) {
            isBlocked = false
            usageTimeAccumulated = 0L // Reinicia el contador
            // OJO: No reiniciamos lastForegroundTimestamp aquí.
            // Se reiniciará solo cuando se detecte que la app no estaba en primer plano y vuelve a estarlo.
        }
    }


    companion object {
        val usageLimit = TimeUnit.MINUTES.toMillis(1)      // Tiempo permitido
        val blockDuration = TimeUnit.MINUTES.toMillis(1)   // Tiempo de bloqueo
    }
}

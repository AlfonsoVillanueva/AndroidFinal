package com.villanueva.proyectofinal

import java.util.concurrent.TimeUnit

data class BlockedAppData(
    val packageName: String,
    var isBlocked: Boolean = false,
    var lastStateChangeTime: Long = System.currentTimeMillis(),
    var nextChangeTime: Long = System.currentTimeMillis() + blockDuration
) {
    fun updateState() {
        isBlocked = !isBlocked
        lastStateChangeTime = System.currentTimeMillis()
        nextChangeTime = if (blockDuration > 0) lastStateChangeTime + blockDuration else 0L
    }

    companion object {
        val blockDuration = TimeUnit.MINUTES.toMillis(1)      // 15 minutos en milisegundos
    }
}
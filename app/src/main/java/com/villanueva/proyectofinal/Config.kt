package com.villanueva.proyectofinal

object Config {
    const val USO_PERMITIDO_MINUTOS = 1
    const val TIEMPO_BLOQUEO_MINUTOS = 1

    val MAX_USAGE_TIME_MILLIS = USO_PERMITIDO_MINUTOS * 60 * 1000L
    val BLOCK_DURATION_MILLIS = TIEMPO_BLOQUEO_MINUTOS * 60 * 1000L
}

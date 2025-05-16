package com.villanueva.proyectofinal

data class AppUsageInfo(
    var tiempoUsoEnMilis: Long = 0L,
    var tiempoUltimaVezVisible: Long = 0L,
    var bloqueadaHasta: Long = 0L,
    var ultimaHoraDesbloqueo: Long = 0L

)

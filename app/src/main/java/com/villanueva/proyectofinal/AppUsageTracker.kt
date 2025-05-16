package com.villanueva.proyectofinal

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.SharedPreferences

object AppUsageTracker {
    private const val PREFS_NAME = "AppUsagePrefs"
    private const val KEY_PREFIX_LAST_BLOCK = "last_block_"

    fun getUsageTime(context: Context, packageName: String): Long {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()
        val startTime = endTime - (1000 * 60 * 60) // Última hora

        val stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)
        for (stat in stats) {
            if (stat.packageName == packageName) {
                return stat.totalTimeInForeground
            }
        }
        return 0L
    }

    fun isInBlockPeriod(context: Context, paqueteActual: String): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val bloqueadaHasta = prefs.getLong(KEY_PREFIX_LAST_BLOCK + paqueteActual, 0L)
        val ahora = System.currentTimeMillis()
        return ahora < bloqueadaHasta
    }


    fun startBlockPeriod(context: Context, packageName: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val bloqueadaHasta = System.currentTimeMillis() + Config.BLOCK_DURATION_MILLIS
        prefs.edit().putLong(KEY_PREFIX_LAST_BLOCK + packageName, bloqueadaHasta).apply()
    }

    fun endBlockPeriod(context: Context, packageName: String) {
        val prefs = context.getSharedPreferences("AppUsagePrefs", Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_PREFIX_LAST_BLOCK + packageName).apply()
    }

    // Aquí la función que necesitas:
    fun getRemainingBlockTime(context: Context, packageName: String): Long {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val bloqueadaHasta = prefs.getLong(KEY_PREFIX_LAST_BLOCK + packageName, 0L)
        val ahora = System.currentTimeMillis()
        return if (bloqueadaHasta > ahora) bloqueadaHasta - ahora else 0L
    }
}

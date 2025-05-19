package com.villanueva.proyectofinal

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Clase de utilidad para manejar las aplicaciones seleccionadas
 * Puedes usar esta clase desde cualquier parte de tu aplicación
 */

object SelectedAppsManager {
    private const val PREFS_NAME = "AppSelectionPrefs"
    private const val SELECTED_APPS_KEY = "selectedAppsData"

    private val gson = Gson()

    private val type = object : TypeToken<MutableList<BlockedAppData>>() {}.type

    fun getSelectedAppDataList(context: Context): List<BlockedAppData> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(SELECTED_APPS_KEY, null)
        val list = if (json != null) {
            gson.fromJson<MutableList<BlockedAppData>>(json, type).toList()
        } else {
            emptyList()
        }

        val packageManager = context.packageManager

        return list.map {
            var appName: String
            var appIcon: Drawable?
            try {
                val applicationInfo = packageManager.getApplicationInfo(it.packageName, 0)
                appName = packageManager.getApplicationLabel(applicationInfo).toString()
                appIcon = packageManager.getApplicationIcon(applicationInfo)
            } catch (e: Exception) {
                appName = it.packageName
                appIcon = ContextCompat.getDrawable(context, android.R.drawable.sym_def_app_icon)
            }

            val updatedApp = it.copy(AppName = appName, icon = appIcon!!)

            if (updatedApp.isBlocked && System.currentTimeMillis() >= updatedApp.unblockAtTimestamp) {
                updatedApp.copy(
                    isBlocked = false,
                    unblockAtTimestamp = 0L,
                    usageTimeAccumulated = 0L,
                    lastForegroundTimestamp = 0L,
                    lastBlockStatusChangeTimestamp = System.currentTimeMillis()
                )
            } else {
                updatedApp
            }
        }
    }


    // Agregar o eliminar paquete individualmente
    fun saveAppSelection(context: Context, packageName: String, isSelected: Boolean) {
        val appList = getSelectedAppDataList(context).toMutableList()
        val packageManager = context.packageManager

        if (isSelected) {
            if (appList.none { it.packageName == packageName }) {
                val appName = try {
                    val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
                    packageManager.getApplicationLabel(applicationInfo).toString()
                } catch (e: Exception) {
                    packageName // fallback en caso de error
                }

                val appIcon = try {
                    val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
                    packageManager.getApplicationIcon(applicationInfo)
                } catch (e: Exception) {
                    androidx.core.content.ContextCompat.getDrawable(context, android.R.drawable.sym_def_app_icon)!!
                }

                appList.add(BlockedAppData(packageName = packageName, AppName = appName, icon = appIcon))
            }
        } else {
            appList.removeAll { it.packageName == packageName }
        }

        saveAppDataList(context, appList)
    }

    // Guardar lista completa (usado internamente)
    fun saveAppDataList(context: Context, appList: List<BlockedAppData>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = gson.toJson(appList)
        prefs.edit().putString(SELECTED_APPS_KEY, json).apply()
    }

    fun isAppSelected(context: Context, packageName: String): Boolean {
        return getSelectedAppDataList(context).any { it.packageName == packageName }
    }

    fun clearAllSelections(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(SELECTED_APPS_KEY).apply()
    }
}

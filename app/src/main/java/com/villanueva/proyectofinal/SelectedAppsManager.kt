package com.villanueva.proyectofinal

import android.content.Context
import android.content.SharedPreferences
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
        return if (json != null) {
            gson.fromJson<MutableList<BlockedAppData>>(json, type).toList() // devolver copia inmutable
        } else {
            emptyList()
        }
    }

    // Agregar o eliminar paquete individualmente
    fun saveAppSelection(context: Context, packageName: String, isSelected: Boolean) {
        val appList = getSelectedAppDataList(context).toMutableList()

        if (isSelected) {
            if (appList.none { it.packageName == packageName }) {
                appList.add(BlockedAppData(packageName))
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

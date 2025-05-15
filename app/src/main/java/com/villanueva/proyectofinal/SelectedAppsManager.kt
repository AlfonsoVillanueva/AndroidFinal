package com.villanueva.proyectofinal

import android.content.Context
import android.content.SharedPreferences

/**
 * Clase de utilidad para manejar las aplicaciones seleccionadas
 * Puedes usar esta clase desde cualquier parte de tu aplicación
 */
object SelectedAppsManager {
    private const val PREFS_NAME = "AppSelectionPrefs"
    private const val SELECTED_APPS_KEY = "selectedApps"

    /**
     * Obtiene la lista de paquetes de aplicaciones seleccionadas
     */
    fun getSelectedApps(context: Context): Set<String> {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getStringSet(SELECTED_APPS_KEY, setOf()) ?: setOf()
    }

    /**
     * Guarda una aplicación como seleccionada
     */
    fun saveAppSelection(context: Context, packageName: String, isSelected: Boolean) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val selectedApps = getSelectedApps(context).toMutableSet()

        if (isSelected) {
            selectedApps.add(packageName)
        } else {
            selectedApps.remove(packageName)
        }

        prefs.edit().putStringSet(SELECTED_APPS_KEY, selectedApps).apply()
    }

    /**
     * Verifica si una aplicación está seleccionada
     */
    fun isAppSelected(context: Context, packageName: String): Boolean {
        return getSelectedApps(context).contains(packageName)
    }

    /**
     * Elimina todas las selecciones (reset)
     */
    fun clearAllSelections(context: Context) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putStringSet(SELECTED_APPS_KEY, setOf()).apply()
    }
}
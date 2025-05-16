package com.villanueva.proyectofinal

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SegundoFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvHeader: TextView
    private lateinit var tvEmptyState: TextView
    private lateinit var preferences: SharedPreferences
    private val PREFS_NAME = "AppSelectionPrefs"
    private val SELECTED_APPS_KEY = "selectedApps"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_segundo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.selectedAppsRecyclerView)
        tvHeader = view.findViewById(R.id.tvHeader)
        tvEmptyState = view.findViewById(R.id.tvEmptyState)
        preferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        setupRecyclerView()
    }

    // Aseguramos que se actualice la lista cuando el usuario regresa a este fragmento
    override fun onResume() {
        super.onResume()
        loadSelectedApps()
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        loadSelectedApps()
    }

    private fun loadSelectedApps() {
        val selectedAppsList = getSelectedApps()

        // Actualizar la UI basado en si hay apps seleccionadas o no
        if (selectedAppsList.isEmpty()) {
            tvHeader.text = "Actualmente no se encuentra ninguna app"
            tvEmptyState.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            tvHeader.text = "Apps bloqueadas"
            tvEmptyState.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE

            // Configurar el adaptador con las apps seleccionadas
            recyclerView.adapter = SelectedAppsAdapter(
                selectedAppsList,
                { packageName -> removeAppFromSelection(packageName) }
            )
        }
    }

    private fun getSelectedApps(): List<SelectedAppInfo> {
        val pm = requireContext().packageManager
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val selectedAppsPackages = preferences.getStringSet(SELECTED_APPS_KEY, setOf()) ?: setOf()

        return selectedAppsPackages.mapNotNull { packageName ->
            try {
                val appInfo = pm.getApplicationInfo(packageName, 0)
                val packageInfo = pm.getPackageInfo(packageName, 0)

                SelectedAppInfo(
                    name = appInfo.loadLabel(pm).toString(),
                    packageName = packageName,
                    icon = appInfo.loadIcon(pm),
                    versionName = packageInfo.versionName ?: "N/A",
                    installDate = dateFormat.format(Date(packageInfo.firstInstallTime))
                )
            } catch (e: Exception) {
                // Si la app ya no está instalada, la ignoramos
                null
            }
        }.sortedBy { it.name.lowercase() }
    }

    private fun removeAppFromSelection(packageName: String) {
        val selectedApps = preferences.getStringSet(SELECTED_APPS_KEY, setOf())?.toMutableSet() ?: mutableSetOf()

        if (selectedApps.contains(packageName)) {
            selectedApps.remove(packageName)
            preferences.edit().putStringSet(SELECTED_APPS_KEY, selectedApps).apply()

            // Actualizar la UI
            Toast.makeText(requireContext(), "App eliminada de la lista", Toast.LENGTH_SHORT).show()
            loadSelectedApps()
        }
    }

    // Modelo de datos para las apps seleccionadas
    data class SelectedAppInfo(
        val name: String,
        val packageName: String,
        val icon: Drawable,
        val versionName: String,
        val installDate: String
    )

    // Adaptador para las apps seleccionadas
    inner class SelectedAppsAdapter(
        private val apps: List<SelectedAppInfo>,
        private val onRemoveClick: (String) -> Unit
    ) : RecyclerView.Adapter<SelectedAppsAdapter.SelectedAppViewHolder>() {

        inner class SelectedAppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val appIcon: ImageView = itemView.findViewById(R.id.appIcon)
            private val appName: TextView = itemView.findViewById(R.id.appName)
            private val packageName: TextView = itemView.findViewById(R.id.packageName)
            private val appVersion: TextView = itemView.findViewById(R.id.appVersion)
            private val btnRemoveApp: ImageButton = itemView.findViewById(R.id.btnRemoveApp)

            fun bind(app: SelectedAppInfo) {
                appIcon.setImageDrawable(app.icon)
                appName.text = app.name
                packageName.text = app.packageName
                appVersion.text = "Versión: ${app.versionName}"

                btnRemoveApp.setOnClickListener {
                    onRemoveClick(app.packageName)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectedAppViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_selected_app, parent, false)
            return SelectedAppViewHolder(view)
        }

        override fun onBindViewHolder(holder: SelectedAppViewHolder, position: Int) {
            holder.bind(apps[position])
        }

        override fun getItemCount(): Int = apps.size
    }

    companion object {
        @JvmStatic
        fun newInstance() = SegundoFragment()
    }
}
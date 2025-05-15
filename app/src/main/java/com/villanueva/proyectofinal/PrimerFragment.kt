package com.villanueva.proyectofinal

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PrimerFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_primer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.appsRecyclerView)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        loadInstalledApps()
    }

    private fun loadInstalledApps() {
        val appsList = getInstalledApps()

        if (appsList.isEmpty()) {
            showEmptyState()
        } else {
            recyclerView.adapter = AppsAdapter(appsList) { app ->
                launchAppOrShowDetails(app)
            }
        }
    }

    private fun getInstalledApps(): List<AppInfo> {
        val pm = requireContext().packageManager
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        // Lista de paquetes de apps comunes que siempre queremos incluir
        val commonSystemApps = setOf(
            "com.android.vending",       // Play Store
            "com.google.android.gm",     // Gmail
            "com.google.android.youtube", // YouTube
            "com.google.android.apps.maps", // Maps
            "com.android.chrome"         // Chrome
        )

        return pm.getInstalledApplications(PackageManager.MATCH_ALL)
            .asSequence()
            .filter { app ->
                val isUserApp = (app.flags and ApplicationInfo.FLAG_SYSTEM) == 0
                val isCommonSystemApp = app.packageName in commonSystemApps
                val hasLauncherIntent = hasLauncherIntent(pm, app.packageName)

                (isUserApp && hasLauncherIntent) || isCommonSystemApp
            }
            .mapNotNull { app ->
                try {
                    val packageInfo = pm.getPackageInfo(app.packageName, 0)
                    AppInfo(
                        name = app.loadLabel(pm).toString(),
                        packageName = app.packageName,
                        icon = app.loadIcon(pm),
                        versionName = packageInfo.versionName ?: "N/A",
                        installDate = dateFormat.format(Date(packageInfo.firstInstallTime))
                    )
                } catch (e: Exception) {
                    null
                }
            }
            .sortedBy { it.name.lowercase() }
            .toList()
    }

    private fun hasLauncherIntent(pm: PackageManager, packageName: String): Boolean {
        val launchIntent = pm.getLaunchIntentForPackage(packageName)
        return launchIntent != null
    }

    private fun showEmptyState() {
        Toast.makeText(
            requireContext(),
            "No se encontraron aplicaciones instaladas",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun launchAppOrShowDetails(app: AppInfo) {
        try {
            val launchIntent = requireContext().packageManager.getLaunchIntentForPackage(app.packageName)
            if (launchIntent != null) {
                startActivity(launchIntent)
            } else {
                showAppDetails(app)
            }
        } catch (e: Exception) {
            showAppDetails(app)
        }
    }

    private fun showAppDetails(app: AppInfo) {
        Toast.makeText(
            requireContext(),
            "${app.name}\nPaquete: ${app.packageName}\nVersión: ${app.versionName}",
            Toast.LENGTH_LONG
        ).show()
    }

    data class AppInfo(
        val name: String,
        val packageName: String,
        val icon: Drawable,
        val versionName: String,
        val installDate: String
    )

    inner class AppsAdapter(
        private val apps: List<AppInfo>,
        private val onItemClick: (AppInfo) -> Unit
    ) : RecyclerView.Adapter<AppsAdapter.AppViewHolder>() {

        inner class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val appIcon: ImageView = itemView.findViewById(R.id.appIcon)
            private val appName: TextView = itemView.findViewById(R.id.appName)
            private val packageName: TextView = itemView.findViewById(R.id.packageName)
            private val appVersion: TextView = itemView.findViewById(R.id.appVersion)
            private val installDate: TextView = itemView.findViewById(R.id.installDate)

            fun bind(app: AppInfo) {
                appIcon.setImageDrawable(app.icon)
                appName.text = app.name
                packageName.text = app.packageName
                appVersion.text = "Versión: ${app.versionName}"
                installDate.text = "Instalado: ${app.installDate}"

                // Configurar todos los textos en negro
                val textColor = Color.BLACK
                appName.setTextColor(textColor)
                packageName.setTextColor(textColor)
                appVersion.setTextColor(textColor)
                installDate.setTextColor(textColor)

                itemView.setOnClickListener { onItemClick(app) }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_app, parent, false)
            return AppViewHolder(view)
        }

        override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
            holder.bind(apps[position])
        }

        override fun getItemCount(): Int = apps.size
    }

    companion object {
        @JvmStatic
        fun newInstance() = PrimerFragment()
    }
}
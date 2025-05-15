package com.villanueva.proyectofinal

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AppsAdapter(
    private val apps: List<AppInfo>,
    private val onItemClickListener: (AppInfo) -> Unit
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
            appVersion.text = if (app.versionName.isNotEmpty()) "Versión: ${app.versionName}" else ""
            installDate.text = if (app.installDate.isNotEmpty()) "Instalado: ${app.installDate}" else ""

            val (textColor, secondaryColor) = if (app.isSystemApp) {
                Color.GRAY to Color.LTGRAY
            } else {
                Color.BLACK to Color.DKGRAY
            }

            appName.setTextColor(textColor)
            packageName.setTextColor(secondaryColor)
            appVersion.setTextColor(secondaryColor)
            installDate.setTextColor(secondaryColor)

            itemView.setOnClickListener { onItemClickListener(app) }
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

data class AppInfo(
    val name: String,
    val packageName: String,
    val icon: Drawable,
    val versionName: String = "N/A",
    val installDate: String = "Desconocida",
    val isSystemApp: Boolean = false
)
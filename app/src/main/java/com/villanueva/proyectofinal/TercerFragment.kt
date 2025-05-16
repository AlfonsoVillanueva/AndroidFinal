package com.villanueva.proyectofinal

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*
import android.app.usage.UsageStatsManager
import android.app.usage.UsageStats
import com.bumptech.glide.Glide

class TercerFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tercer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val imageView = view.findViewById<ImageView>(R.id.gifImageView)

// Cargar el GIF con Glide
        Glide.with(this)
            .asGif()
            .load(R.drawable.gato) // Reemplaza "tu_gif" con el nombre del archivo .gif en res/drawable
            .into(imageView)


        recyclerView = view.findViewById(R.id.appsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        if (!hasUsageStatsPermission(requireContext())) {
            Toast.makeText(requireContext(), "Por favor, activa permiso de acceso al uso de apps", Toast.LENGTH_LONG).show()
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        } else {
            loadUsageData()
        }
    }

    private fun hasUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun loadUsageData() {
        val usageStatsManager = requireContext().getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()

        val stats: List<UsageStats> = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )

        if (stats.isEmpty()) {
            Toast.makeText(requireContext(), "No se encontraron datos de uso. Verifica permiso.", Toast.LENGTH_LONG).show()
            return
        }

        val pm = requireContext().packageManager

        // Sumamos tiempo de uso por app en la última semana
        val usageMap = mutableMapOf<String, Long>()

        stats.forEach {
            usageMap[it.packageName] = (usageMap[it.packageName] ?: 0L) + it.totalTimeInForeground
        }

        // Ordenar por tiempo descendente y tomar top 5
        val topApps = usageMap.entries
            .sortedByDescending { it.value }
            .take(15)
            .mapNotNull { entry ->
                try {
                    val appInfo = pm.getApplicationInfo(entry.key, 0)
                    val icon = pm.getApplicationIcon(appInfo)
                    val name = pm.getApplicationLabel(appInfo).toString()
                    AppUsage(name, icon, entry.value)
                } catch (e: Exception) {
                    null
                }
            }

        recyclerView.adapter = AppsAdapter(topApps)
    }

    data class AppUsage(val name: String, val icon: Drawable, val usageTimeMs: Long)

    inner class AppsAdapter(private val apps: List<AppUsage>) :
        RecyclerView.Adapter<AppsAdapter.AppViewHolder>() {

        inner class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val iconView: ImageView = itemView.findViewById(R.id.appIcon)
            private val nameView: TextView = itemView.findViewById(R.id.appName)
            private val usageView: TextView = itemView.findViewById(R.id.appUsageTime)

            fun bind(app: AppUsage) {
                iconView.setImageDrawable(app.icon)
                nameView.text = app.name
                usageView.text = formatUsageTime(app.usageTimeMs)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_app2, parent, false)
            return AppViewHolder(view)
        }

        override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
            holder.bind(apps[position])
        }

        override fun getItemCount(): Int = apps.size
    }

    private fun formatUsageTime(ms: Long): String {
        val seconds = ms / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val remMinutes = minutes % 60

        return when {
            hours > 0 -> "${hours}h ${remMinutes}m"
            minutes > 0 -> "${minutes}m"
            else -> "${seconds}s"
        }
    }
}



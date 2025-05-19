package com.villanueva.proyectofinal

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SegundoFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private var blockedAppsList: List<BlockedAppData> = emptyList()

    private val blockedAppsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.getStringExtra("blockedAppsJson")?.let { json ->
                val gson = Gson()
                val type = object : TypeToken<List<BlockedAppData>>() {}.type
                blockedAppsList = gson.fromJson(json, type)

                val currentTime = System.currentTimeMillis()
                Log.d("SegundoFragment", "Broadcast recibido con ${blockedAppsList.size} apps:")
                blockedAppsList.forEach { app ->
                    val iconStatus = if (app.icon != null) "Icono recibido: ${app.icon::class.java.simpleName}" else "Icono nulo"
                    if (app.isBlocked) {
                        val remainingBlock = app.getRemainingBlockTime(currentTime)
                        Log.d(
                            "SegundoFragment",
                            "App: ${app.packageName} (${app.AppName}), bloqueada=true, tiempo restante bloqueo: $remainingBlock ms, $iconStatus"
                        )
                    } else {
                        val remainingUsage = app.getRemainingUsageTime()
                        Log.d(
                            "SegundoFragment",
                            "App: ${app.packageName} (${app.AppName}), bloqueada=false, tiempo restante uso: $remainingUsage ms, $iconStatus"
                        )
                    }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("SegundoFragment", "onCreateView ejecutado") // <- Verifica si entra aquí
        return inflater.inflate(R.layout.fragment_segundo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("SegundoFragment", "onViewCreated ejecutado")

        blockedAppsList = SelectedAppsManager.getSelectedAppDataList(requireContext())
        val currentTime = System.currentTimeMillis()
        Log.d("SegundoFragment", "Lista inicial cargada con ${blockedAppsList.size} apps:")
        blockedAppsList.forEach { app ->
            val iconStatus = if (app.icon != null) "Icono recibido: ${app.icon::class.java.simpleName}" else "Icono nulo"
            if (app.isBlocked) {
                val remainingBlock = app.getRemainingBlockTime(currentTime)
                Log.d(
                    "SegundoFragment",
                    "App: ${app.packageName} (${app.AppName}), bloqueada=true, tiempo restante bloqueo: $remainingBlock ms, $iconStatus"
                )
            } else {
                val remainingUsage = app.getRemainingUsageTime()
                Log.d(
                    "SegundoFragment",
                    "App: ${app.packageName} (${app.AppName}), bloqueada=false, tiempo restante uso: $remainingUsage ms, $iconStatus"
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter("com.villanueva.proyectofinal.BLOCKED_APPS_UPDATE")
        ContextCompat.registerReceiver(
            requireActivity(),
            blockedAppsReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onPause() {
        super.onPause()
        requireActivity().unregisterReceiver(blockedAppsReceiver)
    }
}
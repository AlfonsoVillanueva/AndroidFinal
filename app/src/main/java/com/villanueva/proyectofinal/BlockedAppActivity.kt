package com.villanueva.proyectofinal

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class BlockedAppActivity : AppCompatActivity() {

    private lateinit var textView: TextView
    private val handler = Handler(Looper.getMainLooper())
    private var paqueteActual: String = ""


    private val updateRunnable = object : Runnable {
        override fun run() {
            actualizarTiempoRestante()
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blocked_app)

        paqueteActual = intent.getStringExtra("paqueteBloqueado") ?: ""


        // Mostrar el GIF
        val imageView = findViewById<ImageView>(R.id.gif2imageView)
        Glide.with(this)
            .asGif()
            .load(R.drawable.gato)
            .into(imageView)

        val tiempoRestante = AppUsageTracker.getRemainingBlockTime(this, paqueteActual)
        textView = findViewById(R.id.textView)
        textView.text = "Tiempo restante: ${formatTiempo(tiempoRestante)}"


        val btnVolver = findViewById<Button>(R.id.closeButton)
        btnVolver.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        handler.post(updateRunnable)

    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateRunnable)
    }

    private fun actualizarTiempoRestante() {
        val tiempoRestante = AppUsageTracker.getRemainingBlockTime(this, paqueteActual)
        val segundos = (tiempoRestante / 1000) % 60
        val minutos = (tiempoRestante / (1000 * 60)) % 60
        val tiempoFormateado = String.format("%02d:%02d", minutos, segundos)

        textView.text = "⏳ Tiempo restante: $tiempoFormateado"

        if (tiempoRestante <= 0) {
            finish() // Cierra la pantalla de bloqueo automáticamente
        }
    }

    private fun obtenerPaqueteActual(): String {
        val prefs = getSharedPreferences("AppUsagePrefs", Context.MODE_PRIVATE)
        return prefs.getString("current_blocked_app", "") ?: ""
    }

    private fun formatTiempo(millis: Long): String {
        val segundos = (millis / 1000) % 60
        val minutos = (millis / (1000 * 60)) % 60
        return String.format("%02d:%02d", minutos, segundos)
    }
}

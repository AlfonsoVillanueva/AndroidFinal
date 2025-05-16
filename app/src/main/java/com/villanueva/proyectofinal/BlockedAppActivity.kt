package com.villanueva.proyectofinal

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class BlockedAppActivity : AppCompatActivity() {

    private var countDownTimer: CountDownTimer? = null
    private lateinit var timeRemainingTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blocked_app)

        // Mostrar el GIF
        val imageView = findViewById<ImageView>(R.id.gif2imageView)
        Glide.with(this)
            .asGif()
            .load(R.drawable.gato)
            .into(imageView)

        timeRemainingTextView = findViewById(R.id.tv_Time)

        val btnVolver = findViewById<Button>(R.id.closeButton)
        btnVolver.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        // Obtener tiempo restante en milisegundos pasado desde el intent
        val remainingMillis = intent.getLongExtra("remainingTimeMillis", 0L)

        if (remainingMillis > 0) {
            startCountDown(remainingMillis)
        } else {
            // Si no hay tiempo, muestra texto o cierra la actividad
            timeRemainingTextView.text = "Tiempo agotado"
        }
    }

    private fun startCountDown(millis: Long) {
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(millis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = (millisUntilFinished / 1000) / 60
                val seconds = (millisUntilFinished / 1000) % 60
                timeRemainingTextView.text = String.format("%02d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                timeRemainingTextView.text = "00:00"
                finish() // Cierra la pantalla de bloqueo cuando termine el tiempo
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}

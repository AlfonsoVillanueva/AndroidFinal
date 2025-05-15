package com.villanueva.proyectofinal

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class BlockedAppActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blocked_app)

        val closeButton = findViewById<Button>(R.id.closeButton)
        closeButton.setOnClickListener {
            finishAffinity()
        }
    }


}

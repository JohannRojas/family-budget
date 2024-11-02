package com.example.familybudget.activities

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import com.example.familybudget.R
import com.google.firebase.auth.FirebaseAuth

// AjustesActivity.kt
class SettingsActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var toolbar: Toolbar
    private lateinit var toolbarTitle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        auth = FirebaseAuth.getInstance()

        // Configurar Toolbar
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Configurar título centrado
        toolbarTitle = findViewById(R.id.toolbar_title)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbarTitle.text = "Ajustes"

        val btnLogout = findViewById<LinearLayout>(R.id.btnLogout)
        btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
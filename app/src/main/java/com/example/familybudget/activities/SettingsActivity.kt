package com.example.familybudget.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import com.example.familybudget.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class SettingsActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var toolbar: Toolbar
    private lateinit var toolbarTitle: TextView
    private lateinit var tvUsername: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        auth = FirebaseAuth.getInstance()

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbarTitle = findViewById(R.id.toolbar_title)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbarTitle.text = "Ajustes"

        tvUsername = findViewById(R.id.tvUsername)

        val user: FirebaseUser? = auth.currentUser
        user?.let {
            val name = it.displayName
            Log.d("SettingsActivity", "Username: $name")
            tvUsername.text = if (name.isNullOrBlank()) "Usuario" else name
        }

        val btnLogout = findViewById<LinearLayout>(R.id.btnLogout)
        btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

    }
        override fun onSupportNavigateUp(): Boolean {
            onBackPressed()
            return true
        }
}
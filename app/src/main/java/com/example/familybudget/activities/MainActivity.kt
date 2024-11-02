package com.example.familybudget.activities

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.viewpager2.widget.ViewPager2
import com.example.familybudget.R
import com.example.familybudget.adapters.ViewPagerAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp

class MainActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var bottomNavView: BottomNavigationView
    private lateinit var viewPager: ViewPager2
    private lateinit var toolbarTitle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        FirebaseApp.initializeApp(this)
        setContentView(R.layout.activity_main)

        // Configurar Toolbar
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Configurar título centrado
        toolbarTitle = findViewById(R.id.toolbar_title)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Configurar ViewPager2
        viewPager = findViewById(R.id.viewPager)
        val adapter = ViewPagerAdapter(this)
        viewPager.adapter = adapter
        viewPager.offscreenPageLimit = 3

        // Configurar Bottom Navigation View
        bottomNavView = findViewById(R.id.bottomNavigationView)
        bottomNavView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_presupuesto -> viewPager.currentItem = 0
                R.id.navigation_gastos -> viewPager.currentItem = 1
                R.id.navigation_ingresos -> viewPager.currentItem = 2
            }
            true
        }

        // Sincronizar el título del Toolbar y el Bottom Navigation View al deslizar
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                bottomNavView.menu.getItem(position).isChecked = true
                // Actualizar el título del Toolbar
                when (position) {
                    0 -> toolbarTitle.text = "Presupuesto"
                    1 -> toolbarTitle.text = "Gastos"
                    2 -> toolbarTitle.text = "Ingresos"
                }
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
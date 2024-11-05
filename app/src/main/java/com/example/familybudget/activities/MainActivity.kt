package com.example.familybudget.activities

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.viewpager2.widget.ViewPager2
import com.example.familybudget.R
import com.example.familybudget.adapters.ViewPagerAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var bottomNavView: BottomNavigationView
    private lateinit var viewPager: ViewPager2
    private lateinit var toolbarTitle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)
        setContentView(R.layout.activity_main)

        val auth = FirebaseAuth.getInstance()

        if (auth.currentUser == null) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }
        val database = FirebaseDatabase.getInstance()

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        toolbarTitle = findViewById(R.id.toolbar_title)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        viewPager = findViewById(R.id.viewPager)
        val adapter = ViewPagerAdapter(this)
        viewPager.adapter = adapter
        viewPager.offscreenPageLimit = 3

        bottomNavView = findViewById(R.id.bottomNavigationView)
        bottomNavView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_presupuesto -> viewPager.currentItem = 0
                R.id.navigation_gastos -> viewPager.currentItem = 1
                R.id.navigation_ingresos -> viewPager.currentItem = 2
            }
            true
        }

        val rootView = findViewById<View>(R.id.root_layout)
        rootView.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = Rect()
            rootView.getWindowVisibleDisplayFrame(rect)
            val screenHeight = rootView.rootView.height
            val keypadHeight = screenHeight - rect.bottom

            if (keypadHeight > screenHeight * 0.15) {
                bottomNavView.visibility = View.GONE
            } else {
                bottomNavView.visibility = View.VISIBLE
            }
        }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                bottomNavView.menu.getItem(position).isChecked = true
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
package com.example.familybudget

import android.os.Bundle
import android.widget.TextView
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.familybudget.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

//        val toolbar = binding.toolbar.findViewById<Toolbar>(R.id.custom_toolbar)
//        setSupportActionBar(toolbar)

//        supportActionBar?.setDisplayShowTitleEnabled(true)


        val navController = findNavController(R.id.nav_host_fragment_activity_main)
//        // Passing each menu ID as a set of Ids because each
//        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_budget, R.id.navigation_bills, R.id.navigation_income
            )
        )
//        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        navView.isItemHorizontalTranslationEnabled = true

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val title = destination.label.toString()
//            toolbar.findViewById<TextView>(R.id.toolbar_title).text = title
        }
//
//        toolbar.findViewById<TextView>(R.id.toolbar_title).text = "Budget"
    }
}
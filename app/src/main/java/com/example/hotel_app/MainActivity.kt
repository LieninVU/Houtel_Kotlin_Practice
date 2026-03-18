package com.example.hotel_app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.NavOptions
import com.example.hotel_app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Ручная настройка bottom-nav: стабильнее, чем setupWithNavController,
        // и гарантирует переход на Dashboard по клику.
        binding.bottomNav.setOnItemSelectedListener { item ->
            val options = NavOptions.Builder()
                .setLaunchSingleTop(true)
                .setRestoreState(true)
                .setPopUpTo(navController.graph.startDestinationId, false, saveState = true)
                .build()

            return@setOnItemSelectedListener try {
                navController.navigate(item.itemId, null, options)
                true
            } catch (e: IllegalArgumentException) {
                false
            }
        }

        binding.bottomNav.setOnItemReselectedListener { /* no-op */ }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val item = binding.bottomNav.menu.findItem(destination.id)
            if (item != null) item.isChecked = true
        }
    }
}

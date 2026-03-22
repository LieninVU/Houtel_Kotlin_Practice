package com.hotel.app.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.hotel.app.R
import com.hotel.app.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Настройка навигации
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        
        // Настройка BottomNavigationView
        binding.bottomNav.setupWithNavController(navController)
        
        // Настройка AppBarConfiguration для корректной работы с кнопкой "Назад"
        // Указываем топ-уровневые фрагменты, для которых не показывается кнопка "Назад" в ActionBar
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.reviewsFragment,
                R.id.servicesFragment,
                R.id.bookedServicesFragment
            )
        )
        
        // Настройка ActionBar с навигацией
        setupActionBarWithNavController(navController, appBarConfiguration)
        
        // Дополнительная настройка для синхронизации заголовка
        navController.addOnDestinationChangedListener { _, destination, _ ->
            // Можно установить заголовок для ActionBar в зависимости от destination
            supportActionBar?.title = when (destination.id) {
                R.id.reviewsFragment -> "Отзывы"
                R.id.servicesFragment -> "Услуги"
                R.id.bookedServicesFragment -> "Мои бронирования"
                else -> "Отель"
            }
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        // Обработка кнопки "Назад" в ActionBar
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
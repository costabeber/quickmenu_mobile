package com.QuickMenu.mobile.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.QuickMenu.mobile.auth.AuthActivity
// Importa o binding do layout da Activity (que agora deve conter apenas o FragmentContainerView)
import com.QuickMenu.mobile.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navController: NavController


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navHostFragment = supportFragmentManager
            .findFragmentById(binding.navHost.id) as NavHostFragment
        navController = navHostFragment.navController

        binding.navbar.setupWithNavController(navController)

    }

    fun navigateToAuth() {
        // Lógica de navegação de Activity para Activity
        val intent = Intent(this, AuthActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish() // Fecha AuthActivity
    }

}

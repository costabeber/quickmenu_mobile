package com.QuickMenu.mobile.main

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI.onNavDestinationSelected
import androidx.navigation.ui.onNavDestinationSelected
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

        criaNavBar()

    }

    fun criaNavBar() {
        val navView = binding.navbar

        navView.setOnItemSelectedListener { item ->
            onNavDestinationSelected(item, navController)
            true
        }

        navView.setOnItemReselectedListener { item ->
            val builder = NavOptions.Builder()
                .setPopUpTo(navController.graph.startDestinationId, false)
                .setLaunchSingleTop(true)
            val options = builder.build()

            try {

                navController.navigate(item.itemId, null, options)
            } catch (e: IllegalArgumentException) {
                // Handle the exception here
            }
        }

        // Opcional, mas recomendado: Sincronizar o item selecionado quando a pilha de volta muda
        // Este listener continua igual e é importante para manter o ícone correto destacado
        navController.addOnDestinationChangedListener { _, destination, _ ->
            navView.menu.findItem(destination.id)?.isChecked = true
        }
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


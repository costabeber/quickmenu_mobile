package com.QuickMenu.mobile.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.QuickMenu.mobile.databinding.ActivityAuthBinding
import com.QuickMenu.mobile.main.MainActivity

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        // Inflar o layout usando o View Binding
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }

    fun navigateToMain() {
        // Lógica de navegação de Activity para Activity
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish() // Fecha AuthActivity
    }
}
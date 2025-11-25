package com.QuickMenu.mobile.auth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import com.QuickMenu.mobile.R

class HomeFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_home)

        val btnFavoritos: Button = findViewById(R.id.btn_favoritos)
        val btnPromocoes: Button = findViewById(R.id.btn_promocoes)
        val btnTendencias: Button = findViewById(R.id.btn_tendencias)

        val imgItem1: ImageButton = findViewById(R.id.img_item_1)
        val imgItem2: ImageButton = findViewById(R.id.img_item_2)
        val imgItem3: ImageButton = findViewById(R.id.img_item_3)

        val imgSweetCake: ImageButton = findViewById(R.id.img_sweet_cake)
        val imgCigarrete: ImageButton = findViewById(R.id.img_cigarrete)
        val imgCantina: ImageButton = findViewById(R.id.img_cantina)

        val btnQrCode: ImageButton = findViewById(R.id.btn_qr_code)

        val btnHome: ImageButton = findViewById(R.id.btn_home)
        val btnCart: ImageButton = findViewById(R.id.btn_cart)
        val btnNotifications: ImageButton = findViewById(R.id.btn_notifications)
        val btnProfile: ImageButton = findViewById(R.id.btn_profile)

        val emptyListener = View.OnClickListener { }

        btnFavoritos.setOnClickListener(emptyListener)
        btnPromocoes.setOnClickListener(emptyListener)
        btnTendencias.setOnClickListener(emptyListener)

        imgItem1.setOnClickListener(emptyListener)
        imgItem2.setOnClickListener(emptyListener)
        imgItem3.setOnClickListener(emptyListener)

        imgSweetCake.setOnClickListener(emptyListener)
        imgCigarrete.setOnClickListener(emptyListener)
        imgCantina.setOnClickListener(emptyListener)

        btnQrCode.setOnClickListener(emptyListener)

        btnHome.setOnClickListener(emptyListener)
        btnCart.setOnClickListener(emptyListener)
        btnNotifications.setOnClickListener(emptyListener)
        btnProfile.setOnClickListener(emptyListener)
    }
}
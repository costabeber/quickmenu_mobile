package com.QuickMenu.mobile.main.home

data class ItemRestaurante(
    val id: String = "",
    val nome: String = "",
    val descricao: String = "",
    val imageUrl: String? = null,
    var isFavorito: Boolean = false
)

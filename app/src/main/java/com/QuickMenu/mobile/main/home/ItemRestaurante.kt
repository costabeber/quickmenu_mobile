package com.QuickMenu.mobile.main.home

data class ItemRestaurante(
    var id: String = "",
    var userId: String = "",
    val nome: String = "",
    val descricao: String = "",
    val imageUrl: String? = null,
    var isFavorito: Boolean = false
)

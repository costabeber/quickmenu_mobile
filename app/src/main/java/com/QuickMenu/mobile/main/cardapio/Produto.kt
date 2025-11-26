package com.QuickMenu.mobile.main.cardapio

data class Produto(
    var produtoId: String = "",
    val nome: String = "",
    val descricao: String = "",
    val preco: Double = 0.0,
    val imageUrl: String = ""
)
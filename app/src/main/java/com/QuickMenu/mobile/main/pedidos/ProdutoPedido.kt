package com.QuickMenu.mobile.main.pedidos

data class ProdutoPedido(
    val produtoId: String = "",
    val nome: String = "",
    val preco: Double = 0.0,
    val quantidade: Int = 0,
    val imageUrl: String = ""
)
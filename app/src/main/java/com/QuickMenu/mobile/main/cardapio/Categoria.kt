package com.QuickMenu.mobile.main.cardapio

data class Categoria(
    val id: String = "",
    val nome: String = "",
    val produtoCardapios: List<ProdutoCardapio> = emptyList()
)

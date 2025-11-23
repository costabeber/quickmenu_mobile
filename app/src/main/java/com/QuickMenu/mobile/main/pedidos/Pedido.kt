package com.QuickMenu.mobile.main.pedidos

data class Pedido(
    val id: String,
    val restauranteId: String,
    val produtoPedidos: List<ProdutoPedido>,
    val precoTotal: Double,
    val status: Status,
    val horarioFormatado: String
)
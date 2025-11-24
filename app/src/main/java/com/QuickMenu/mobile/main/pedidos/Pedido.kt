package com.QuickMenu.mobile.main.pedidos

data class Pedido(
    val id: String,
    val restaurante: String,
    val produtoPedidos: List<ProdutoPedido>,
    val status: Status,
    val horario: String
)
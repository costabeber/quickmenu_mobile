package com.QuickMenu.mobile.main.pedidos

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.QuickMenu.mobile.databinding.ItemPedidoBinding
import java.text.NumberFormat
import java.util.Locale

class PedidosAdapter(
    private val pedidos: List<Pedido>
) : RecyclerView.Adapter<PedidosAdapter.PedidoViewHolder>() {

    class PedidoViewHolder(val binding: ItemPedidoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PedidoViewHolder {
        val binding = ItemPedidoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PedidoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PedidoViewHolder, position: Int) {
        val pedido = pedidos[position]
        with(holder.binding) {
            // Se o ID do restaurante estiver vazio, colocamos um texto padrão
            restaurante.text = if(pedido.restauranteId.isEmpty()) "Restaurante Teste" else pedido.restauranteId

            textTime.text = pedido.horarioFormatado

            // Opcional: Mostrar o total do pedido em algum lugar do card se tiver o TextView
            // val formatador = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
            // txtTotal.text = formatador.format(pedido.precoTotal)

            if (recyclerProdutos.layoutManager == null) {
                recyclerProdutos.layoutManager = LinearLayoutManager(root.context)
                recyclerProdutos.setHasFixedSize(true)
                recyclerProdutos.isNestedScrollingEnabled = false
            }
            recyclerProdutos.adapter = ProdutosAdapter(pedido.produtoPedidos)
        }
    }

    override fun getItemCount(): Int = pedidos.size
}
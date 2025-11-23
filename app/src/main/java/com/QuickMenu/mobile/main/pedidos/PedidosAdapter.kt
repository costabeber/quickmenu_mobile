package com.QuickMenu.mobile.main.pedidos

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.QuickMenu.mobile.databinding.ItemPedidoBinding

class PedidosAdapter(
    private val pedidos: List<Pedido>
) : RecyclerView.Adapter<PedidosAdapter.PedidoViewHolder>() {

    class PedidoViewHolder(val binding: ItemPedidoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PedidoViewHolder {
        val binding = ItemPedidoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PedidoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PedidoViewHolder, position: Int) {
        val pedido = pedidos[position]
        with(holder.binding) {
            restaurante.text = pedido.restaurante
            textTime.text = pedido.horario

            // Só configura o RecyclerView uma vez
            if (recyclerProdutos.layoutManager == null) {
                recyclerProdutos.layoutManager = LinearLayoutManager(root.context)
                recyclerProdutos.setHasFixedSize(true)
                recyclerProdutos.isNestedScrollingEnabled = false
            }

            // Atualiza o adapter sempre com os produtos do pedido atual
            recyclerProdutos.adapter = ProdutosAdapter(pedido.produtoPedidos)
        }
    }

    override fun getItemCount(): Int = pedidos.size
}
package com.QuickMenu.mobile.main.pedidos

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.QuickMenu.mobile.R // Importe seu R
import com.QuickMenu.mobile.databinding.ItemProdutoPedidoBinding

class ProdutosAdapter(
    private val produtoPedidos: List<ProdutoPedido>
) : RecyclerView.Adapter<ProdutosAdapter.ProdutoViewHolder>() {

    class ProdutoViewHolder(val binding: ItemProdutoPedidoBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProdutoViewHolder {
        val binding = ItemProdutoPedidoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ProdutoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProdutoViewHolder, position: Int) {
        val produto = produtoPedidos[position]
        with(holder.binding) {
            nomeProduto.text = produto.nome
            quantidadeProduto.text = "${produto.quantidade}x"

            // Como o banco salva URL (String) e não ID (Int), e ainda não temos Glide/Picasso:
            // Vamos usar a imagem padrão definida no XML ou setar manualmente o placeholder
            imageProduto.setImageResource(R.drawable.produto_default) // Ou bolo, conforme seu projeto
        }
    }

    override fun getItemCount(): Int = produtoPedidos.size
}
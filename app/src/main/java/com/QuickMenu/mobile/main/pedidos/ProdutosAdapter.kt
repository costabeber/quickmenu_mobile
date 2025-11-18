package com.QuickMenu.mobile.main.pedidos

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.QuickMenu.mobile.databinding.ItemProdutoPedidoBinding
class ProdutosAdapter(
    private val produtos: List<Produto>
) : RecyclerView.Adapter<ProdutosAdapter.ProdutoViewHolder>() {

    class ProdutoViewHolder(val binding: ItemProdutoPedidoBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProdutoViewHolder {
        val binding = ItemProdutoPedidoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProdutoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProdutoViewHolder, position: Int) {
        val produto = produtos[position]
        with(holder.binding) {
            nomeProduto.text = produto.nome
            quantidadeProduto.text = "${produto.quantidade}x"
            imageProduto.setImageResource(produto.imagemRes)
        }
    }

    override fun getItemCount(): Int = produtos.size
}

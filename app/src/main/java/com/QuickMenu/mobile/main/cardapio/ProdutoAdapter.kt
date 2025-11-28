package com.QuickMenu.mobile.main.cardapio
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.QuickMenu.mobile.databinding.ItemProdutoBinding

import com.bumptech.glide.Glide
import java.text.NumberFormat
import java.util.Locale

class ProdutoAdapter(
    private val produtos: List<Produto>,
    private val onAddClick: (Produto) -> Unit // Callback para o clique no +
) : RecyclerView.Adapter<ProdutoAdapter.ProdutoViewHolder>() {

    inner class ProdutoViewHolder(val binding: ItemProdutoBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProdutoViewHolder {
        val binding = ItemProdutoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProdutoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProdutoViewHolder, position: Int) {
        val produto = produtos[position]
        with(holder.binding) {
            txtNomeProduto.text = produto.nome
            txtDescricaoProduto.text = produto.descricao

            val formatador = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
            txtPrecoProduto.text = formatador.format(produto.preco)

            // Carregar imagem com Glide
            if (produto.imageUrl.isNotEmpty()) {
                Glide.with(root.context)
                    .load(produto.imageUrl)
                    .centerCrop()
                    .into(imgProduto)
            }

            // Clique no botão de adicionar
            btnAdicionar.setOnClickListener {
                onAddClick(produto)
            }
        }
    }

    override fun getItemCount() = produtos.size
}
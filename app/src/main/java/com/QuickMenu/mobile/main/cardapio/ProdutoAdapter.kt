package com.QuickMenu.mobile.main.cardapio

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.QuickMenu.mobile.databinding.ItemProdutoBinding
import com.bumptech.glide.Glide
import java.text.NumberFormat
import java.util.Locale
class ProdutoAdapter(
    private val produtos: List<ProdutoCardapio>,
    // ⚠️ ALTERADO: Agora é onProdutoClick, não onAddClick
    private val onProdutoClick: (ProdutoCardapio) -> Unit
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

            if (produto.imageUrl.isNotEmpty()) {
                Glide.with(root.context)
                    .load(produto.imageUrl)
                    .centerCrop()
                    .into(imgProduto)
            }

            // ⚠️ ALTERADO: O clique agora abre a tela do produto
            btnAdicionar.setOnClickListener {
                onProdutoClick(produto)
            }

            // Opcional: Se a linha inteira for clicável:
            holder.itemView.setOnClickListener {
                onProdutoClick(produto)
            }
        }
    }

    override fun getItemCount() = produtos.size
}
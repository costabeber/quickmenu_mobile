package com.QuickMenu.mobile.main.carrinho

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.QuickMenu.mobile.R
import com.QuickMenu.mobile.databinding.ItemCarrinhoBinding
import com.bumptech.glide.Glide
import kotlin.inc
import kotlin.toString

class CarrinhoAdapter (private val itens: MutableList<ItemCarrinho>,
                       private val listener: CarrinhoActionsListener)
    : RecyclerView.Adapter<CarrinhoAdapter.CarrinhoViewHolder>() {
    inner class CarrinhoViewHolder(private val binding: ItemCarrinhoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ItemCarrinho) {
            binding.Nome.text = item.nome
            binding.quantidade.text = "${item.quantidade}"
            binding.Preco.text = "R$ ${item.preco}"

            // AQUI: Carrega a imagem do produto, ou o placeholder

            if (!item.imageUrl.isNullOrEmpty()) {
                Glide.with(binding.root.context)
                    .load(item.imageUrl)
                    .placeholder(R.drawable.bolo) // Mostra o bolo enquanto carrega
                    .error(R.drawable.bolo)       // Mostra o bolo se der erro na URL
                    .centerCrop()
                    .into(binding.imagemProduto)
            } else {
                // Se não tiver URL, usa a imagem padrão
                binding.imagemProduto.setImageResource(R.drawable.bolo)
            }

            // Lógica de clique na imagem do produto
            binding.imagemProduto.setOnClickListener {
                // TODO Implemente a navegação aqui
            }

            // Lógica de clique na lixeira
            binding.btnLixo.setOnClickListener {
                listener.onRemoverItem(adapterPosition)
            }

            binding.btnAddQtd.setOnClickListener {
                // 1. Atualiza o objeto localmente
                item.quantidade++

                // 2. Atualiza a UI imediatamente (feedback visual)
                binding.quantidade.text = item.quantidade.toString()

                // 3. Notifica o Fragment para salvar a mudança no Firestore
                listener.onUpdateItem(item)
            }

            // Botão Diminuir Quantidade (-)
            binding.btnSubQtd.setOnClickListener {
                if (item.quantidade > 1) {
                    // Diminuir
                    item.quantidade--
                    binding.quantidade.text = item.quantidade.toString()
                    listener.onUpdateItem(item)
                } else {
                    // Remover (quantidade atinge zero)
                    listener.onRemoverItem(adapterPosition)
                }
            }
        }
    }

    fun adicionarItem(item: ItemCarrinho) {
        itens.add(item)
        // Notifica a RecyclerView que um novo item foi adicionado no final da lista
        notifyItemInserted(itens.size - 1)
    }

    // Onde o layout é criado e a classe de binding é inflada
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarrinhoViewHolder {
        val binding = ItemCarrinhoBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return CarrinhoViewHolder(binding)
    }


    // Onde os dados são associados às views
    override fun onBindViewHolder(holder: CarrinhoViewHolder, position: Int) {
        val itemAtual = itens[position]
        holder.bind(itemAtual)

    }

    // Retorna a quantidade total de itens na lista
    override fun getItemCount(): Int {
        // Retorna o número de objetos (produtoCardapios únicos) na lista
        return itens.size
    }

    fun removerItem(position: Int) {
        // 1. Remove o item da lista de DADOS
        itens.removeAt(position)

        // 2. Notifica a RecyclerView que o item foi removido naquela posição
        notifyItemRemoved(position)

        // NOTA: Você deve chamar notifyItemRemoved() e NÃO notifyDataSetChanged()
        // para obter a animação de remoção suave.
    }


}
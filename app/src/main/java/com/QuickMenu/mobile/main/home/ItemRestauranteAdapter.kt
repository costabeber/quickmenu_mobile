package com.QuickMenu.mobile.main.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.QuickMenu.mobile.databinding.ItemRestauranteBinding
import com.bumptech.glide.Glide

// Data Class atualizada (apenas para referência)
/*
data class ItemRestaurante(
    val name: String,
    val type: String,
    val imageUrl: String? // String para a URL
)
*/

class ItemRestauranteAdapter(private var restaurants: MutableList<ItemRestaurante>) :
    RecyclerView.Adapter<ItemRestauranteAdapter.ItemRestauranteViewHolder>() {

    // (ViewHolder e onCreateViewHolder permanecem inalterados)

    class ItemRestauranteViewHolder(val binding: ItemRestauranteBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemRestauranteViewHolder {
        val binding = ItemRestauranteBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ItemRestauranteViewHolder(binding)
    }

    // 3. onBindViewHolder: Liga os dados usando a instância do Binding.
    override fun onBindViewHolder(holder: ItemRestauranteViewHolder, position: Int) {
        val item = restaurants[position]
        val context = holder.binding.root.context // Pega o Contexto para usar com o Glide

        // Uso direto do ID da View via Binding:
        holder.binding.tvRestaurantName.text = item.nome
        holder.binding.tvRestaurantType.text = item.descricao

        // 🚀 NOVO: Usando o Glide para carregar a imagem da URL
        Glide.with(context)
            .load(item.imageUrl) // Carrega a URL
            // Opcional: Adicionar um placeholder enquanto a imagem carrega
           .placeholder(com.QuickMenu.mobile.R.drawable.sweetcake)
            // Opcional: Adicionar uma imagem de erro
            .error(com.QuickMenu.mobile.R.drawable.bolo)
            .into(holder.binding.imgRestaurant) // ImageView onde a imagem será exibida

        holder.binding.root.setOnClickListener {
            // TODO: Lógica de clique do restaurante.
        }
    }

    // (getItemCount e updateList permanecem inalterados)

    override fun getItemCount() = restaurants.size

    fun updateList(newRestaurants: MutableList<ItemRestaurante>) {
        restaurants = newRestaurants
        notifyDataSetChanged()
    }
}
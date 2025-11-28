package com.QuickMenu.mobile.main.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.QuickMenu.mobile.R // Import para usar os ícones de favorito
import com.QuickMenu.mobile.databinding.ItemRestauranteBinding
import com.bumptech.glide.Glide

// 1. CONSTRUTOR ATUALIZADO
//    Agora, ele recebe não apenas a lista, mas também duas "funções de callback".
//    - onFavoriteClick: será chamada quando o ícone de coração for clicado.
//    - onItemClick: será chamada quando qualquer outra parte do item for clicada.
class ItemRestauranteAdapter(
    private var restaurants: MutableList<ItemRestaurante>,
    private val onFavoriteClick: (ItemRestaurante) -> Unit,
    private val onItemClick: (ItemRestaurante) -> Unit
) : RecyclerView.Adapter<ItemRestauranteAdapter.ItemRestauranteViewHolder>() {

    // O ViewHolder interno não precisa de alterações.
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

    // 2. ONBINDVIEWHOLDER ATUALIZADO
    //    É aqui que a mágica acontece. Ligamos os dados de cada restaurante à sua representação visual.
    override fun onBindViewHolder(holder: ItemRestauranteViewHolder, position: Int) {
        val item = restaurants[position]
        val context = holder.binding.root.context

        // Preenche os textos com os dados do restaurante
        holder.binding.tvRestaurantName.text = item.nome
        holder.binding.tvRestaurantType.text = item.descricao

        // Carrega a imagem da internet usando a biblioteca Glide
        Glide.with(context)
            .load(item.imageUrl)
            .placeholder(R.drawable.sweetcake) // Imagem mostrada enquanto carrega
            .error(R.drawable.bolo)             // Imagem mostrada se der erro
            .into(holder.binding.imgRestaurant)

        // 3. LÓGICA DO ÍCONE DE FAVORITO
        //    Verifica se o campo 'isFavorito' do item é verdadeiro ou falso
        //    e define o ícone de coração (preenchido ou com borda) correspondente.
        val favoriteIcon = if (item.isFavorito) {
            R.drawable.ic_favorite_filled // Coração preenchido
        } else {
            R.drawable.ic_favorite_border // Coração com borda
        }
        holder.binding.ivFavorite.setImageResource(favoriteIcon)

        // 4. CONFIGURAÇÃO DOS CLIQUES
        //    Aqui, associamos as funções que recebemos no construtor aos cliques nas views.
        holder.binding.ivFavorite.setOnClickListener {
            // Quando o ícone de coração for clicado, chama a função 'onFavoriteClick'
            // e passa o 'item' atual como parâmetro para o HomeFragment saber qual foi.
            onFavoriteClick(item)
        }

        holder.itemView.setOnClickListener {
            // Quando o card inteiro for clicado, chama a função 'onItemClick'
            // e passa o 'item' para o HomeFragment saber qual restaurante abrir.
            onItemClick(item)
        }
    }

    override fun getItemCount() = restaurants.size

    // 5. MÉTODO DE ATUALIZAÇÃO DA LISTA
    //    Este método permite que o HomeFragment envie uma nova lista de restaurantes
    //    e o conjunto de IDs favoritos para o adapter.
    fun updateList(newRestaurants: List<ItemRestaurante>, favoriteIds: Set<String>) {
        // Antes de atualizar a lista, percorremos cada restaurante da nova lista
        // e definimos seu campo 'isFavorito' como 'true' se o ID dele estiver
        // no conjunto 'favoriteIds'.
        newRestaurants.forEach { restaurante ->
            restaurante.isFavorito = favoriteIds.contains(restaurante.id)
        }

        // Limpa a lista antiga
        restaurants.clear()
        // Adiciona todos os itens da nova lista (já com o estado de favorito atualizado)
        restaurants.addAll(newRestaurants)
        // Notifica o RecyclerView que os dados mudaram, para que ele redesenhe a tela.
        notifyDataSetChanged()
    }
}

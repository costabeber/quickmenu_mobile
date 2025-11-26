import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.QuickMenu.mobile.databinding.ItemRestauranteBinding // Verifique o nome real do seu pacote e binding!

// Data Class (Apenas para referência no Adapter)
data class ItemRestaurante(
    val name: String,
    val type: String,
    val imageResId: Int
)

class ItemRestauranteAdapter(private var restaurants: MutableList<ItemRestaurante>) :
    RecyclerView.Adapter<ItemRestauranteAdapter.ItemRestauranteViewHolder>() {

    // 1. ViewHolder: Agora armazena a instância do Binding
    class ItemRestauranteViewHolder(val binding: ItemRestauranteBinding) :
        RecyclerView.ViewHolder(binding.root) // O 'root' é a View principal do item

    // 2. onCreateViewHolder: Infla o layout usando o Binding.
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

        // Uso direto do ID da View via Binding:
        holder.binding.tvRestaurantName.text = item.name      // Assumindo ID tv_restaurant_name
        holder.binding.tvRestaurantType.text = item.type      // Assumindo ID tv_restaurant_type
        holder.binding.imgRestaurant.setImageResource(item.imageResId) // Assumindo ID img_restaurant

        holder.binding.root.setOnClickListener {
            // TODO: Lógica de clique do restaurante.
        }
    }

    // 4. getItemCount: Retorna o número total de itens.
    override fun getItemCount() = restaurants.size

    fun updateList(newRestaurants: MutableList<ItemRestaurante>) {
        restaurants = newRestaurants
        notifyDataSetChanged()
    }
}
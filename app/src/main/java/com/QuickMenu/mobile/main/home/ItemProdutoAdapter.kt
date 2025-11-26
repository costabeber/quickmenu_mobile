import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlin.collections.mutableListOf
import com.QuickMenu.mobile.databinding.ItemProdutoBinding // Verifique o nome real do seu pacote e binding!
import com.QuickMenu.mobile.main.home.ItemProduto

// Data Class (Apenas para referência no Adapter)
/*data class ItemProduto(
    val price: String,
    val imageResId: Int
)*/

class ItemProdutoAdapter(private var products:  MutableList<ItemProduto>  /* mutableListOf<ItemProduto>*/):
    RecyclerView.Adapter<ItemProdutoAdapter.ItemProdutoViewHolder>() {

    // 1. ViewHolder: Agora armazena a instância do Binding
    class ItemProdutoViewHolder(val binding: ItemProdutoBinding) :
        RecyclerView.ViewHolder(binding.root) // O 'root' é a View principal do item

    // 2. onCreateViewHolder: Infla o layout usando o Binding.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemProdutoViewHolder {
        val binding = ItemProdutoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ItemProdutoViewHolder(binding)
    }

    // 3. onBindViewHolder: Liga os dados usando a instância do Binding.
    override fun onBindViewHolder(holder: ItemProdutoViewHolder, position: Int) {
        val item = products[position]

        // Uso direto do ID da View via Binding:
        holder.binding.tvPrice.text = item.price
        holder.binding.imgItem.setImageResource(item.imageResId) // Assumindo IDs tv_price e img_item

        holder.binding.root.setOnClickListener {
            // TODO: Lógica de clique do produto.
        }
    }

    // 4. getItemCount: Retorna o número total de itens.
    override fun getItemCount() = products.size

    fun updateList(newProducts: MutableList<ItemProduto>) {
        products = newProducts
        notifyDataSetChanged()
    }
}
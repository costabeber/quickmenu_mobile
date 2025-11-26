package com.QuickMenu.mobile.main.home

import ItemProdutoAdapter
import ItemRestauranteAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.QuickMenu.mobile.R
import com.QuickMenu.mobile.databinding.FragmentHomeBinding
import ItemRestaurante
import com.QuickMenu.mobile.main.home.ItemProduto

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var recentAdapter: ItemProdutoAdapter
    private lateinit var restaurantAdapter: ItemRestauranteAdapter

    // --- DADOS INICIAIS (Nomes ajustados nas Data Classes) ---
    // Simulação: A lista de restaurantes é mutável para permitir a filtragem
    private val allRestaurants = mutableListOf<ItemRestaurante>(
        ItemRestaurante("Sweet Cake", "Doceria", R.drawable.sweetcake),
        ItemRestaurante("Cigarrete burguer", "Hamburgueria", R.drawable.burger),
    )
    // Simulação: Itens recentes
    private val recentItems = mutableListOf(
        ItemProduto("R$ 8,00", R.drawable.pao),
        ItemProduto("R$ 30,00", R.drawable.pao),
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecentItems()
        setupRestaurantList()
        setupSearchBar()
        setupFilterButtons()
    }

    // --- SETUP DO CARROSSEL HORIZONTAL (Produtos Recentes) ---
    private fun setupRecentItems() {
        // Inicializa o ItemProdutoAdapter
        recentAdapter = ItemProdutoAdapter(recentItems)
        binding.recyclerItemProduto.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = recentAdapter
        }
    }

    // --- SETUP DA LISTA VERTICAL (Restaurantes) ---
    private fun setupRestaurantList() {
        // Inicializa o ItemRestauranteAdapter
        restaurantAdapter = ItemRestauranteAdapter(allRestaurants)
        binding.recyclerRestaurantList.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = restaurantAdapter
            // isNestedScrollingEnabled = false // Mantido para melhor rolagem se necessário
        }
    }

    // --- LÓGICA DE PESQUISA (Search Bar) ---
    private fun setupSearchBar() {
        binding.searchBar.doOnTextChanged { text, _, _, _ ->
            val query = text.toString()
            filterRestaurants(query)
            filterRecentItems(query)
        }

        // Lógica de ação do teclado (clique no botão 'Buscar' do teclado)
        binding.searchBar.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                // Você pode adicionar aqui uma lógica de pesquisa mais pesada (ex: chamada de API)
                filterRestaurants(binding.searchBar.text.toString())
                return@setOnEditorActionListener true
            }
            false
        }
    }

    // --- FILTRO DE RESTAURANTES ---
    private fun filterRestaurants(query: String) {
        val filteredList = if (query.isEmpty()) {
            allRestaurants
        } else {
            // Filtra se o nome OU o tipo contêm o texto da busca (ignorando caixa)
            allRestaurants.filter {
                it.name.contains(query, ignoreCase = true) || it.type.contains(query, ignoreCase = true)
            }
        }
        // Atualiza a lista exibida no RecyclerView
        restaurantAdapter.updateList(filteredList.toMutableList())
    }

    // --- FILTRO DE ITENS RECENTES (Implementação Simples) ---
    private fun filterRecentItems(query: String) {
        // Se a pesquisa for muito genérica, você pode querer exibir uma lista diferente.
        val filteredList = if (query.isEmpty()) {
            recentItems
        } else {
            // Exemplo: filtrar itens recentes por preço.
            recentItems.filter {
                it.price.contains(query, ignoreCase = true)
            }
        }
        recentAdapter.updateList(filteredList.toMutableList())
    }

    // --- CONFIGURAÇÃO DOS BOTÕES DE FILTRO ---
    private fun setupFilterButtons() {
        binding.btnFavoritos.setOnClickListener {
            // TODO: Implementar lógica de filtragem para "Favoritos"
            // filterRestaurantsByCategory("Favoritos")
        }
        binding.btnPromocoes.setOnClickListener {
            // TODO: Implementar lógica de filtragem para "Promoções"
        }
        binding.btnTendencias.setOnClickListener {
            // TODO: Implementar lógica de filtragem para "Tendências"
        }
    }

    // --- LIMPEZA DE BINDING ---
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Evita vazamento de memória
    }
}
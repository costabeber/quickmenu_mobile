package com.QuickMenu.mobile.main.home

import ItemProdutoAdapter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.QuickMenu.mobile.R
import com.QuickMenu.mobile.databinding.FragmentHomeBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var recentAdapter: ItemProdutoAdapter
    private lateinit var restaurantAdapter: ItemRestauranteAdapter
    private lateinit var db: FirebaseFirestore

    private var allRestaurants = mutableListOf<ItemRestaurante>()

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
        db = Firebase.firestore

        setupRecentItems()
        setupRestaurantList()
        loadRestaurantsFromFirestore()
        setupSearchBar()
        setupFilterButtons()
    }

    private fun loadRestaurantsFromFirestore() {
        // Usamos collectionGroup para buscar em todas as subcoleções "restaurantes"
        db.collectionGroup("restaurantes").get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    Log.d("HomeFragment", "Nenhum restaurante encontrado com collectionGroup.")
                    // Opcional: mostrar uma mensagem para o usuário
                    return@addOnSuccessListener
                }
                allRestaurants.clear()
                for (document in result) {
                    Log.d("HomeFragment", "Restaurante encontrado: ${document.id}")

                    // Você pode voltar a usar toObject se a sua data class estiver correta.
                    // Isso é mais limpo e menos propenso a erros.
                    val restaurante = document.toObject(ItemRestaurante::class.java)
                    allRestaurants.add(restaurante)
                }
                restaurantAdapter.updateList(allRestaurants)
            }
            .addOnFailureListener { exception ->
                // Se a query falhar, procure a URL para criar o índice no log de erro!
                Log.w("HomeFragment", "Erro ao buscar grupo de coleção 'restaurantes'.", exception)
                Toast.makeText(context, "Erro ao carregar restaurantes. Verifique o log.", Toast.LENGTH_LONG).show()
            }
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
        // Inicializa o ItemRestauranteAdapter com a lista vazia
        restaurantAdapter = ItemRestauranteAdapter(mutableListOf())
        binding.recyclerRestaurantList.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = restaurantAdapter
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
            allRestaurants.filter {
                it.nome.contains(query, ignoreCase = true) || it.descricao.contains(query, ignoreCase = true)
            }
        }
        restaurantAdapter.updateList(filteredList.toMutableList())
    }

    // --- FILTRO DE ITENS RECENTES (Implementação Simples) ---
    private fun filterRecentItems(query: String) {
        val filteredList = if (query.isEmpty()) {
            recentItems
        } else {
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
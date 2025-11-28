package com.QuickMenu.mobile.main.home

import ItemProdutoHomeAdapter
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.QuickMenu.mobile.R
import com.QuickMenu.mobile.databinding.FragmentHomeBinding
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var recentAdapter: ItemProdutoHomeAdapter
    private lateinit var restaurantAdapter: ItemRestauranteAdapter
    private lateinit var db: FirebaseFirestore

    private var isFilteredByFavorites = false

    private var allRestaurants = mutableListOf<ItemRestaurante>()
    private var lastSearchedRestaurants = mutableListOf<ItemRestaurante>()
    private var favoriteRestaurants = mutableSetOf<String>() // Armazena os IDs dos favoritos

    // Simulação: Itens recentes
    private val recentItems = mutableListOf(
        ItemProdutoHome("R$ 8,00", R.drawable.pao),
        ItemProdutoHome("R$ 30,00", R.drawable.pao),
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

        loadFavorites() // Carrega os favoritos salvos
        setupRecentItems()
        setupRestaurantList()
        loadRestaurantsFromFirestore()
        setupSearchBar()
        setupFilterButtons()

        val backPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Verifica se o filtro de favoritos está ativo
                if (isFilteredByFavorites) {
                    // Se estiver, desativa o filtro e volta para a lista principal
                    isFilteredByFavorites = false
                    filterAndDisplayRestaurants("")

                    // Mostra uma mensagem opcional para o usuário
                    Toast.makeText(context, "Exibindo todos os restaurantes", Toast.LENGTH_SHORT).show()
                } else {
                    // Se não estiver, executa a ação padrão do botão voltar (ex: sair do app)
                    // Para isso, desabilitamos temporariamente nosso callback e chamamos o back de novo
                    isEnabled = false
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                    isEnabled = true // Reabilita para futuras interceptações
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backPressedCallback)
    }

    // --- CARREGAMENTO DE DADOS ---

    private fun loadRestaurantsFromFirestore() {
        db.collectionGroup("restaurantes").get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    Log.d("HomeFragment", "Nenhum restaurante encontrado.")
                    return@addOnSuccessListener
                }
                allRestaurants.clear()
                for (document in result) {
                    val restaurante = document.toObject(ItemRestaurante::class.java).copy(id = document.id)
                    allRestaurants.add(restaurante)
                }
                // Exibe a lista completa inicialmente
                filterAndDisplayRestaurants("")
                Log.d("HomeFragment", "${allRestaurants.size} restaurantes carregados.")
            }
            .addOnFailureListener { exception ->
                Log.w("HomeFragment", "Erro ao buscar restaurantes.", exception)
                Toast.makeText(context, "Erro ao carregar restaurantes.", Toast.LENGTH_LONG).show()
            }
    }

    private fun loadFavorites() {
        // Usar o nome padrão para consistência
        val prefs = activity?.getSharedPreferences("RestaurantPreferences", Context.MODE_PRIVATE) ?: return
        favoriteRestaurants = prefs.getStringSet("favorite_ids", emptySet())?.toMutableSet() ?: mutableSetOf()
    }

    private fun saveFavorites() {
        // Usar o nome padrão para consistência
        val prefs = activity?.getSharedPreferences("RestaurantPreferences", Context.MODE_PRIVATE) ?: return
        with(prefs.edit()) {
            putStringSet("favorite_ids", favoriteRestaurants)
            apply()
        }
    }



    // --- SETUP DOS COMPONENTES DA UI ---

    private fun setupRecentItems() {
        recentAdapter = ItemProdutoHomeAdapter(recentItems)
        binding.recyclerItemProduto.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = recentAdapter
        }
    }

    private fun setupRestaurantList() {
        // Inicializa o adapter e define os callbacks de clique
        restaurantAdapter = ItemRestauranteAdapter(mutableListOf(),
            onFavoriteClick = { restaurante: ItemRestaurante ->
                toggleFavorite(restaurante)
            },
            onItemClick = { restaurante ->
                // Adiciona o restaurante clicado à lista de "últimos pesquisados"
                updateLastSearched(restaurante)
                // TODO: Navegar para a tela de detalhes do restaurante
                Toast.makeText(context, "Clicou em: ${restaurante.nome}", Toast.LENGTH_SHORT).show()
            }
        )

        binding.recyclerRestaurantList.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = restaurantAdapter
        }
    }

    private fun setupSearchBar() {
        binding.searchBar.doOnTextChanged { text, _, _, _ ->
            val query = text.toString().trim()
            filterAndDisplayRestaurants(query)
        }
    }

    // --- LÓGICA DE FILTRO E EXIBIÇÃO ---

    private fun filterAndDisplayRestaurants(query: String) {
        val filteredList = if (query.isEmpty()) {
            // Se a busca está vazia, mostra os últimos pesquisados e depois o resto
            (lastSearchedRestaurants + allRestaurants).distinctBy { it.id }
        } else {
            // Filtra a lista completa com base na query
            allRestaurants.filter {
                it.nome.contains(query, ignoreCase = true)
                        //|| it.descricao.contains(query, ignoreCase = true)
            }
        }

        // 2. Agora, ORDENAMOS a lista filtrada.
        // O método 'sortedByDescending' cria uma nova lista ordenada.
        // A condição 'favoriteRestaurants.contains(it.id)' retorna 'true' para favoritos e 'false' para não favoritos.
        // Em Kotlin, 'true' é considerado "maior" que 'false', então 'sortedByDescending'
        // coloca todos os itens que resultam em 'true' (os favoritos) no início.
        val sortedList = filteredList.sortedByDescending { favoriteRestaurants.contains(it.id) }

        // 3. Finalmente, enviamos a lista já filtrada E ordenada para o adapter.
        restaurantAdapter.updateList(sortedList, favoriteRestaurants)
    }

    private fun filterRecentItems(query: String) {
        val filteredList = if (query.isEmpty()) {
            recentItems
        } else {
            recentItems.filter { it.price.contains(query, ignoreCase = true) }
        }
        recentAdapter.updateList(filteredList.toMutableList())
    }
    private fun setupFilterButtons() {
        binding.btnFavoritos.setOnClickListener {
            isFilteredByFavorites = true
            val favoritedList = allRestaurants.filter { favoriteRestaurants.contains(it.id) }
            // Ao filtrar por favoritos, todos na lista são favoritos.
            restaurantAdapter.updateList(favoritedList, favoriteRestaurants)
        }

        binding.btnPromocoes.setOnClickListener {
            // TODO: Lógica para promoções
        }
        binding.btnTendencias.setOnClickListener {
            // TODO: Lógica para tendências
        }
        binding.root.setOnClickListener { // Renomeado de root para um botão mais claro
            // Ao redefinir, mostramos a lista inicial completa
            filterAndDisplayRestaurants("")
        }
    }




    // --- LÓGICA DE NEGÓCIO ---

    private fun toggleFavorite(restaurante: ItemRestaurante) {
        val isCurrentlyFavorite = favoriteRestaurants.contains(restaurante.id)
        if (isCurrentlyFavorite) {
            favoriteRestaurants.remove(restaurante.id)
            Log.d("HomeFragment", "${restaurante.nome} removido dos favoritos.")
        } else {
            favoriteRestaurants.add(restaurante.id)
            Log.d("HomeFragment", "${restaurante.nome} adicionado aos favoritos.")
        }
        saveFavorites()

        // Pega a query atual da barra de busca para refiltrar a lista com o novo estado de favorito
        val currentQuery = binding.searchBar.text.toString().trim()
        filterAndDisplayRestaurants(currentQuery)
    }



    private fun updateLastSearched(restaurante: ItemRestaurante) {
        // 1. Remove o restaurante da lista se ele já existir, para evitar duplicatas
        lastSearchedRestaurants.removeIf { it.id == restaurante.id }

        // 2. Adiciona o restaurante clicado no INÍCIO (posição 0) da lista
        lastSearchedRestaurants.add(0, restaurante)

        // 3. Garante que a lista não tenha mais de 5 itens (ou o número que preferir).
        //    Usar .take() é mais seguro e evita o crash, pois cria uma nova lista.
        if (lastSearchedRestaurants.size > 5) {
            lastSearchedRestaurants = lastSearchedRestaurants.take(5).toMutableList()
        }

        // 4. Salva a lista de IDs no SharedPreferences para o UsuarioFragment usar
        val prefs = activity?.getSharedPreferences("RestaurantPreferences", Context.MODE_PRIVATE) ?: return
        with(prefs.edit()) {
            // Pega os IDs, junta em uma string separada por vírgula e salva
            val idsString = lastSearchedRestaurants.joinToString(separator = ",") { it.id }
            putString("last_selected_ids", idsString)
            apply() // Salva as alterações
        }

        Log.d("HomeFragment", "Últimos pesquisados (salvos): ${lastSearchedRestaurants.map { it.nome }}")

        // 5. Atualiza a exibição na tela do HomeFragment para refletir a nova ordem dos "últimos pesquisados"
        filterAndDisplayRestaurants(binding.searchBar.text.toString().trim())
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

package com.QuickMenu.mobile.main.home

import ItemProdutoHomeAdapter
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
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

        voltar()
    }

    private fun voltar(){

        val navController = findNavController()
        val isCardapio = navController.previousBackStackEntry?.destination?.id == R.id.cardapioFragment

        val backPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isCardapio) {
                    isEnabled = false
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                    isEnabled = true
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
                    // Converte o JSON para o objeto (preenche nome, imagem, etc)
                    val restaurante = document.toObject(ItemRestaurante::class.java)

                    // Preenche o ID do restaurante
                    restaurante.id = document.id


                    // document.reference = caminho completo
                    // .parent = coleção "restaurantes"
                    // .parent = documento do Usuário (Operador)
                    val operadorId = document.reference.parent.parent?.id ?: ""

                    restaurante.userId = operadorId // Guardamos na memória

                    allRestaurants.add(restaurante)
                }

                // Exibe a lista completa inicialmente
                filterAndDisplayRestaurants("")
                Log.d("HomeFragment", "${allRestaurants.size} restaurantes carregados.")
            }
            .addOnFailureListener { exception ->
                Log.w("HomeFragment", "Erro ao buscar restaurantes.", exception)
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
        restaurantAdapter = ItemRestauranteAdapter(mutableListOf(),
            onFavoriteClick = { restaurante ->
                toggleFavorite(restaurante)
            },
            onItemClick = { restaurante ->
                updateLastSearched(restaurante)



                // Prepara os dados
                val bundle = Bundle().apply {
                    putString("restauranteId", restaurante.id)
                    putString("donoId", restaurante.userId)
                }

                // Navegan usando o ID que está no seu XML de navegação

                try {
                    findNavController().navigate(R.id.cardapioFragment, bundle)
                } catch (e: Exception) {
                    // Esse Log ajuda a identificar se o ID está errado ou se a ação não existe
                    Log.e("HomeFragment", "Erro ao navegar: ${e.message}")
                }
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

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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.QuickMenu.mobile.R
import com.QuickMenu.mobile.databinding.FragmentHomeBinding
import com.QuickMenu.mobile.main.pedidos.ProdutoPedido
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.NumberFormat
import java.util.Locale

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var recentAdapter: ItemProdutoHomeAdapter
    private lateinit var restaurantAdapter: ItemRestauranteAdapter
    private lateinit var db: FirebaseFirestore
    private val auth = Firebase.auth

    private var allRestaurants = mutableListOf<ItemRestaurante>()
    private var lastSearchedRestaurants = mutableListOf<ItemRestaurante>()
    private var favoriteRestaurants = mutableSetOf<String>()

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

        setupRecyclerViews()
        loadFavorites()
        loadRestaurantsFromFirestore()
        fetchRecentOrderedProducts()

        setupSearchBar()
        setupFilterButtons()
        voltar()
    }

    private fun setupRecyclerViews() {
        // Setup Recentes (Horizontal)
        recentAdapter = ItemProdutoHomeAdapter(emptyList())
        binding.recyclerItemProduto.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = recentAdapter
        }

        // Setup Restaurantes
        restaurantAdapter = ItemRestauranteAdapter(mutableListOf(),
            onFavoriteClick = { toggleFavorite(it) },
            onItemClick = { restaurante ->
                updateLastSearched(restaurante)
                val bundle = Bundle().apply {
                    putString("restauranteId", restaurante.id)
                    putString("donoId", restaurante.userId)
                }
                try {
                    findNavController().navigate(R.id.cardapioFragment, bundle)
                } catch (e: Exception) {
                    Log.e("HomeFragment", getString(R.string.log_erro_navegar, e.message))
                }
            }
        )

        binding.recyclerRestaurantList.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = restaurantAdapter
        }
    }

    private fun fetchRecentOrderedProducts() {
        val userId = auth.currentUser?.uid ?: return

        lifecycleScope.launch {
            try {

                val pedidosSnapshot = db.collection("Usuario")
                    .document(userId)
                    .collection("Pedidos")
                    .orderBy("dataPedido", Query.Direction.DESCENDING)
                    .limit(5)
                    .get()
                    .await()

                val todosProdutosRecentes = mutableListOf<ItemProdutoHome>()
                val nomesAdicionados = mutableSetOf<String>()

                for (doc in pedidosSnapshot.documents) {
                    if (todosProdutosRecentes.size >= 3) break

                    val itensSnapshot = doc.reference.collection("Itens").get().await()

                    for (itemDoc in itensSnapshot.documents) {
                        val produtoBD = itemDoc.toObject(ProdutoPedido::class.java)

                        if (produtoBD != null && !nomesAdicionados.contains(produtoBD.nome)) {

                            val precoFormatado = NumberFormat
                                .getCurrencyInstance(Locale("pt", "BR"))
                                .format(produtoBD.preco)

                            val itemHome = ItemProdutoHome(
                                nome = produtoBD.nome,
                                preco = precoFormatado,
                                imageUrl = produtoBD.imageUrl
                            )

                            todosProdutosRecentes.add(itemHome)
                            nomesAdicionados.add(produtoBD.nome)

                            if (todosProdutosRecentes.size >= 3) break
                        }
                    }
                }

                if (todosProdutosRecentes.isNotEmpty()) {
                    recentAdapter.updateList(todosProdutosRecentes)
                } else {
                    Log.d("HomeFragment", getString(R.string.log_produtos_recentes_vazio))
                }

            } catch (e: Exception) {
                Log.e("HomeFragment", getString(R.string.log_erro_produtos_recentes), e)
            }
        }
    }

    private fun loadRestaurantsFromFirestore() {
        db.collectionGroup("restaurantes").get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) return@addOnSuccessListener

                allRestaurants.clear()
                for (document in result) {
                    val restaurante = document.toObject(ItemRestaurante::class.java)
                    restaurante.id = document.id
                    restaurante.userId = document.reference.parent.parent?.id ?: ""
                    allRestaurants.add(restaurante)
                }

                filterAndDisplayRestaurants("")
            }
    }

    private fun loadFavorites() {
        val prefs = activity?.getSharedPreferences("RestaurantPreferences", Context.MODE_PRIVATE)
            ?: return
        favoriteRestaurants = prefs.getStringSet("favorite_ids", emptySet())
            ?.toMutableSet() ?: mutableSetOf()
    }

    private fun saveFavorites() {
        val prefs = activity?.getSharedPreferences("RestaurantPreferences", Context.MODE_PRIVATE)
            ?: return
        with(prefs.edit()) {
            putStringSet("favorite_ids", favoriteRestaurants)
            apply()
        }
    }

    private fun setupSearchBar() {
        binding.searchBar.doOnTextChanged { text, _, _, _ ->
            filterAndDisplayRestaurants(text.toString().trim())
        }
    }

    private fun setupFilterButtons() {
        binding.btnFavoritos.setOnClickListener {
            val favoritedList = allRestaurants.filter { favoriteRestaurants.contains(it.id) }
            restaurantAdapter.updateList(favoritedList, favoriteRestaurants)
        }

        binding.root.setOnClickListener {
            filterAndDisplayRestaurants("")
        }
    }

    private fun filterAndDisplayRestaurants(query: String) {
        val filteredList = if (query.isEmpty()) {
            (lastSearchedRestaurants + allRestaurants).distinctBy { it.id }
        } else {
            allRestaurants.filter { it.nome.contains(query, ignoreCase = true) }
        }

        val sortedList = filteredList.sortedByDescending { favoriteRestaurants.contains(it.id) }
        restaurantAdapter.updateList(sortedList, favoriteRestaurants)
    }

    private fun toggleFavorite(restaurante: ItemRestaurante) {
        if (favoriteRestaurants.contains(restaurante.id)) {
            favoriteRestaurants.remove(restaurante.id)
        } else {
            favoriteRestaurants.add(restaurante.id)
        }
        saveFavorites()
        filterAndDisplayRestaurants(binding.searchBar.text.toString().trim())
    }

    private fun updateLastSearched(restaurante: ItemRestaurante) {
        lastSearchedRestaurants.removeIf { it.id == restaurante.id }
        lastSearchedRestaurants.add(0, restaurante)
        if (lastSearchedRestaurants.size > 5) {
            lastSearchedRestaurants = lastSearchedRestaurants.take(5).toMutableList()
        }
    }

    private fun voltar() {
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

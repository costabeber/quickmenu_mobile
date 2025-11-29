package com.QuickMenu.mobile.main.cardapio

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.QuickMenu.mobile.databinding.FragmentCardapioBinding
import com.QuickMenu.mobile.main.carrinho.ItemCarrinho
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class CardapioFragment : Fragment() {

    private var _binding: FragmentCardapioBinding? = null
    private val binding get() = _binding!!

    private val db = Firebase.firestore
    private val auth = Firebase.auth

    // Variáveis para guardar os IDs dinâmicos
    private var currentRestauranteId: String = ""
    private var currentDonoId: String = ""

    private val listaCategoriasCompletas = mutableListOf<Categoria>()
    private lateinit var categoriaAdapter: CategoriaAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCardapioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        arguments?.let { bundle ->
            currentRestauranteId = bundle.getString("restauranteId") ?: ""
            currentDonoId = bundle.getString("donoId") ?: ""
        }

        if (currentRestauranteId.isEmpty() || currentDonoId.isEmpty()) {
            Toast.makeText(context, "Erro ao carregar restaurante", Toast.LENGTH_SHORT).show()
            return
        }


        setupRecyclerView()
        carregarDadosRestaurante() // Agora usa as variáveis dinâmicas
        carregarCardapio()         // Agora usa as variáveis dinâmicas
        binding.btnVoltar.setOnClickListener {
            // Isso diz ao NavController para voltar para a tela anterior na pilha.
            // Como você veio da Home, ele voltará para a Home.
            findNavController().navigateUp()
        }
    }


    private fun setupRecyclerView() {
        // Passamos a função de adicionar ao carrinho para o adapter
        categoriaAdapter = CategoriaAdapter(listaCategoriasCompletas) { produtoClicado ->
            adicionarAoCarrinho(produtoClicado)
        }

        binding.rvCategorias.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = categoriaAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun carregarDadosRestaurante() {
        // Usando os IDs dinâmicos
        val restauranteDocRef = db.collection("operadores").document(currentDonoId)
            .collection("restaurantes").document(currentRestauranteId)

        restauranteDocRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val nome = document.getString("nome")
                    val descricao = document.getString("descricao")
                    val imageUrl = document.getString("imageUrl")

                    binding.txtNomeRestaurante.text = nome ?: "Restaurante"
                    binding.txtDescricaoRestaurante.text = descricao

                    if (!imageUrl.isNullOrEmpty()) {
                        Glide.with(this).load(imageUrl).centerCrop().into(binding.imgBanner)
                    }
                } else {
                    Log.e("Cardapio", "Restaurante não encontrado no banco.")
                }
            }
            .addOnFailureListener { e ->
                Log.e("Cardapio", "Erro conexao: $e")
            }
    }

    private fun carregarCardapio() {
        // Caminho dinâmico baseado no clique
        val BASE_PATH = "operadores/$currentDonoId/restaurantes/$currentRestauranteId"

        val pathCategorias = "$BASE_PATH/categorias"
        val pathProduto = "$BASE_PATH/produtos"

        db.collection(pathCategorias).get()
            .addOnSuccessListener { querySnapshot ->
                listaCategoriasCompletas.clear()

                // Verifica se tem categorias
                if (querySnapshot.isEmpty) {
                    // Opcional: Avisar que não tem cardápio
                }

                for (catDoc in querySnapshot) {
                    val catNome = catDoc.getString("nome") ?: "Categoria"

                    catDoc.reference.collection("produtosCategoria").get()
                        .addOnSuccessListener { produtosSnapshot ->

                            val produtosDestaCategoria = mutableListOf<Produto>()
                            val totalProdutosParaBuscar = produtosSnapshot.size()
                            var produtosProcessados = 0

                            if (totalProdutosParaBuscar == 0) {
                                // Se categoria não tem produtos, decide se mostra ou não
                                // organizarEAtualizarLista(...)
                            }

                            for (prodLinkDoc in produtosSnapshot) {
                                val produtoIdReal = prodLinkDoc.id

                                db.collection(pathProduto).document(produtoIdReal).get()
                                    .addOnSuccessListener { prodDetailDoc ->
                                        if (prodDetailDoc.exists()) {
                                            val prodObj = prodDetailDoc.toObject(Produto::class.java)
                                            prodObj?.let {
                                                it.produtoId = produtoIdReal
                                                produtosDestaCategoria.add(it)
                                            }
                                        }
                                        produtosProcessados++
                                        if (produtosProcessados == totalProdutosParaBuscar) {
                                            organizarEAtualizarLista(Categoria(catDoc.id, catNome, produtosDestaCategoria))
                                        }
                                    }
                                    .addOnFailureListener {
                                        produtosProcessados++ // Conta mesmo com erro para não travar
                                        if (produtosProcessados == totalProdutosParaBuscar) {
                                            organizarEAtualizarLista(Categoria(catDoc.id, catNome, produtosDestaCategoria))
                                        }
                                    }
                            }
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Cardapio", "Erro ao carregar categorias", e)
            }
    }

    // Função auxiliar para evitar concorrência desordenada na UI

    private fun organizarEAtualizarLista(novaCategoria: Categoria) {
        listaCategoriasCompletas.add(novaCategoria)
        // Opcional: Ordenar categorias alfabeticamente ou por ordem específica se tiver campo 'ordem'
        // listaCategoriasCompletas.sortBy { it.nome }
        categoriaAdapter.notifyDataSetChanged()
    }

    // 3. Adicionar ao Carrinho (Firestore)
    private fun adicionarAoCarrinho(produto: com.QuickMenu.mobile.main.cardapio.Produto) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(context, "Faça login para adicionar ao carrinho", Toast.LENGTH_SHORT).show()
            return
        }

        val carrinhoRef = db.collection("Usuario").document(userId).collection("Carrinho")
        val docProduto = carrinhoRef.document(produto.produtoId)

        // Verifica se já existe para incrementar quantidade, ou cria novo
        docProduto.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val qtdAtual = document.getLong("quantidade")?.toInt() ?: 0
                docProduto.update("quantidade", qtdAtual + 1)
                Toast.makeText(context, "+1 ${produto.nome}", Toast.LENGTH_SHORT).show()
            } else {
                val novoItem = ItemCarrinho(
                    produtoId = produto.produtoId,
                    nome = produto.nome,
                    preco = produto.preco,
                    quantidade = 1,
                    imageUrl = produto.imageUrl
                )
                docProduto.set(novoItem)
                Toast.makeText(context, "Adicionado ao carrinho!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
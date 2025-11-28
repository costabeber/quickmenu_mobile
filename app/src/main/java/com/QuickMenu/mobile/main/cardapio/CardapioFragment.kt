package com.QuickMenu.mobile.main.cardapio

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
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

    // ID do restaurante fixo conforme solicitado
    private val RESTAURANTE_ID = "81ClpafKYkEvB5HFCCZF"
    private val USER_UID = "81ClpafKYkEvB5HFCCZF"
    // Lista principal que alimentará o adapter
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

        setupRecyclerView()
        carregarDadosRestaurante()
        carregarCardapio()
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

    // 1. Carrega Banner, Nome e Descrição do Restaurante
    private fun carregarDadosRestaurante() {
        // ID do usuário/administrador que contém a lista de restaurantes
        // Vou usar o ID que você forneceu como FIXO
        val USER_ADMIN_ID = "8UtmPeDZdpRd3RCEHTXwsWlALby2"

        // O caminho completo é: collection("users") -> document(USER_ADMIN_ID) -> collection("restaurantes") -> document(RESTAURANTE_ID)

        val restauranteDocRef = db.collection("users").document(USER_ADMIN_ID)
            .collection("restaurantes").document(RESTAURANTE_ID)

        restauranteDocRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val nome = document.getString("nome")
                    val descricao = document.getString("descricao")
                    val imageUrl = document.getString("imageUrl")

                    binding.txtNomeRestaurante.text = nome ?: "Restaurante"
                    binding.txtDescricaoRestaurante.text = descricao

                    if (!imageUrl.isNullOrEmpty()) {
                        // Garante que o Glide é carregado no contexto do Fragment
                        Glide.with(this).load(imageUrl).centerCrop().into(binding.imgBanner)
                    }
                } else {
                    Log.e("Cardapio", "Documento do restaurante não encontrado.")
                    Toast.makeText(context, "Restaurante indisponível.", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("Cardapio", "Erro ao carregar dados do restaurante: $e")
                Toast.makeText(context, "Erro de conexão.", Toast.LENGTH_SHORT).show()
            }
    }
    // Dentro de CardapioFragment, defina:
    private val USER_ADMIN_ID = "8UtmPeDZdpRd3RCEHTXwsWlALby2"

    private fun carregarCardapio() {
        val BASE_PATH = "users/$USER_ADMIN_ID/restaurantes/$RESTAURANTE_ID"

        val pathCategorias = "$BASE_PATH/categorias"
        val pathProduto = "$BASE_PATH/produtos"

        db.collection(pathCategorias).get()
            .addOnSuccessListener { querySnapshot ->
                listaCategoriasCompletas.clear()
                // ... (código de verificação de lista vazia) ...

                for (catDoc in querySnapshot) {
                    // ... (código de pegar ID e Nome) ...

                    catDoc.reference.collection("produtosCategoria").get()
                        .addOnSuccessListener { produtosSnapshot ->

                            val totalProdutosParaBuscar = produtosSnapshot.size()
                            var produtosProcessados = 0
                            val produtosDestaCategoria = mutableListOf<Produto>()

                            // ... (código para categoria vazia) ...

                            for (prodLinkDoc in produtosSnapshot) {
                                val produtoIdReal = prodLinkDoc.id

                                // Agora o pathProduto usa o caminho completo
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
                                            organizarEAtualizarLista(Categoria(catDoc.id, catDoc.getString("nome") ?: "Categoria", produtosDestaCategoria))
                                        }
                                    }
                                    // Tratamento de erro para buscar o produto individual
                                    .addOnFailureListener { e ->
                                        Log.e("Cardapio", "Erro ao buscar produto: $produtoIdReal", e)
                                        // Devemos ainda contar este produto como processado para não travar a UI
                                        produtosProcessados++
                                        if (produtosProcessados == totalProdutosParaBuscar) {
                                            organizarEAtualizarLista(Categoria(catDoc.id, catDoc.getString("nome") ?: "Categoria", produtosDestaCategoria))
                                        }
                                    }
                            }
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Cardapio", "Erro ao carregar categorias", e)
                Toast.makeText(context, "Erro ao carregar cardápio", Toast.LENGTH_SHORT).show()
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
package com.QuickMenu.mobile.main.carrinho

import android.icu.text.NumberFormat
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.QuickMenu.mobile.databinding.FragmentCarrinhoBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import java.util.Locale

// Interface para o Fragment avisar o Adapter sobre remoção
interface CarrinhoActionsListener {
    fun onRemoverItem(position: Int)

    fun onUpdateItem(Item: ItemCarrinho)
}

class CarrinhoFragment : Fragment(), CarrinhoActionsListener {

    private var _binding: FragmentCarrinhoBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var banco: FirebaseFirestore

    private var firestoreListener: ListenerRegistration? = null // Listener em tempo real

    private lateinit var carrinhoAdapter: CarrinhoAdapter
    // Torna a lista de itens uma propriedade da classe
    private val listaItens = mutableListOf<ItemCarrinho>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCarrinhoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = Firebase.auth
        banco = Firebase.firestore

        /*// 1. Inicializa a lista de dados (populando, já que agora é uma propriedade)
        listaItens.add(ItemCarrinho("1", "Bolo Red Velvet", 50.00, 1, "3"))
        listaItens.add(ItemCarrinho("2", "Brigadeiro de morango", 9.00, 1, "3"))*/

        // 2. Cria e INICIALIZA a instância do seu adapter
        carrinhoAdapter = CarrinhoAdapter(listaItens, this)

        // 3. Configura a RecyclerView
        binding.rvCarrinhoItens.layoutManager = LinearLayoutManager(context)
        binding.rvCarrinhoItens.adapter = carrinhoAdapter

        startRealtimeCartListener()

        // 4. Lógica de clique de adição (na Activity)
        binding.btnAddItemTeste.setOnClickListener {
            val novoItem = ItemCarrinho(
                produtoId = "ID_${System.currentTimeMillis()}",
                nome = "Item Adicionado (TESTE)",
                preco = 10.00,
                quantidade = 1,
                imageUrl = ""
            )

            saveOrUpdateCartItem(novoItem)
        }

        // 1. Encontra a Toolbar dentro do layout deste Fragment
        val toolbar = binding.toolbar // **VERIFIQUE O ID DA SUA TOOLBAR NO XML DO CARRINHO**

        // 2. Define a Toolbar do Fragment como a ActionBar da Activity
        (activity as AppCompatActivity).setSupportActionBar(toolbar)

        // 3. Vincula a Toolbar ao NavController do Fragment
        // Isso faz com que:
        // a) O título (label) do nav_graph seja exibido.
        // b) O botão de voltar (seta) apareça.
        // c) O clique no botão de voltar navegue para trás (popBackStack).
        val navController = findNavController()
        toolbar.setupWithNavController(navController)
        (activity as AppCompatActivity).supportInvalidateOptionsMenu()
    }

    private fun getCartRef() =
        auth.currentUser?.uid?.let { uid ->
            banco.collection("Usuario").document(uid).collection("Carrinho")
        }
    // Inicia a leitura e sincronização em tempo real do carrinho do usuário.
    private fun startRealtimeCartListener() {
        val cartRef = getCartRef() ?: run {
            android.util.Log.e("Carrinho", "Usuário não logado. Listener não iniciado.")
            // Opcional: mostrar mensagem de erro na UI
            return
        }

        // addSnapshotListener: Notifica o Fragment sempre que houver mudanças no Firestore
        firestoreListener = cartRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                android.util.Log.e("Carrinho", "Erro ao ouvir o carrinho: $e")
                return@addSnapshotListener
            }

            // Garante que a lista local seja atualizada com o estado exato do Firestore
            listaItens.clear()

            if (snapshot != null && !snapshot.isEmpty) {
                // Converte os documentos do Firestore para a lista de objetos ItemCarrinho
                val novosItens = snapshot.documents.mapNotNull {
                    it.toObject(ItemCarrinho::class.java)
                }
                listaItens.addAll(novosItens)
            }

            // Notifica a RecyclerView e recalcula o total
            carrinhoAdapter.notifyDataSetChanged()
            onTotalChanged(calcularTotal(), listaItens.size)
        }
    }

    fun saveOrUpdateCartItem(item: ItemCarrinho) {
        val cartRef = getCartRef() ?: return // Sai se o usuário não estiver logado

        // Usa o produtoId como ID do documento
        cartRef.document(item.produtoId).set(item)
            .addOnFailureListener { e ->
                android.util.Log.e("Carrinho", "Falha ao salvar item: ${e.message}")
            }
        // Não é necessário onSuccess, pois o Listener (acima) cuida da atualização da UI.
    }

    private fun removeCartItem(produtoId: String) {
        val cartRef = getCartRef() ?: return

        cartRef.document(produtoId).delete()
            .addOnFailureListener { e ->
                android.util.Log.e("Carrinho", "Falha ao remover item: ${e.message}")
            }
        // O Listener em tempo real atualizará a UI automaticamente após a exclusão.
    }

    // Chamado pelo Adapter quando a quantidade de um item muda
    override fun onUpdateItem(item: ItemCarrinho) {
        // Envia o item completo atualizado para ser salvo no Firestore
        saveOrUpdateCartItem(item)
    }

    // Chamado pelo Adapter quando o botão de lixo (ou subtração de item=1) é clicado
    override fun onRemoverItem(position: Int) {
        // 1. Obtém o ID do item a ser removido da lista local (que está sincronizada)
        val itemToRemove = listaItens.getOrNull(position) ?: return

        // 2. Remove do Firestore
        removeCartItem(itemToRemove.produtoId)

        // A remoção visual da lista local será feita pelo Listener
    }
    fun onTotalChanged(novoTotal: Double, totalItens: Int) {

        // 1. Configuração do formatador de moeda (Exemplo: Real Brasileiro)
        val formatador = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

        // 2. ATUALIZAÇÃO DO TOTAL USANDO BINDING
        // Acessa as TextViews diretamente pelo objeto 'binding'
        binding.total.text = formatador.format(novoTotal)

        // 3. ATUALIZAÇÃO DA CONTAGEM DE ITENS USANDO BINDING
        binding.totalTitle.text = "Total ($totalItens)"
    }

    // Função para calcular o total
    private fun calcularTotal(): Double {
        return listaItens.sumOf { it.preco * it.quantidade }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as? AppCompatActivity)?.setSupportActionBar(null)
        _binding = null
    }
}
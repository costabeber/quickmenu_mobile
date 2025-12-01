package com.QuickMenu.mobile.main.produto

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.QuickMenu.mobile.R
import com.QuickMenu.mobile.databinding.FragmentProdutoBinding
import com.bumptech.glide.Glide // Importe a biblioteca de carregamento de imagens (Glide, Picasso, etc.)
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class ProdutoFragment : Fragment() {

    private var _binding: FragmentProdutoBinding? = null
    private val binding get() = _binding!!

    // Variáveis de estado do fragmento
    private var quantidade: Int = 1
    private var precoUnitario: Double = 0.0

    // Variável para armazenar o ID do produto vindo do Cardápio
    private var produtoId: String? = null
    private var donoId: String? = null
    private var nomeProduto: String? = null

    private var descricaoProduto: String? = null // 🆕 Novo
    private var imageUrlProduto: String? = null   // 🆕 Novo

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProdutoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Carregar argumentos passados pelo CardapioFragment
        loadArguments()

        // 2. Simular carregamento de dados do banco e preencher a UI
        loadProductData()

        // 3. Inicializar os listeners
        initListeners()

        // 4. Atualizar o display de preço e quantidade
        updateQuantityDisplay()
    }

    private fun loadArguments() {
        // Assume que você passará estes argumentos ao navegar do CardapioFragment
        arguments?.let {
            produtoId = it.getString("produtoId")
            donoId = it.getString("donoId")
            nomeProduto = it.getString("nomeProduto")
            precoUnitario = it.getDouble("precoUnitario")
            descricaoProduto = it.getString("descricaoProduto")
            imageUrlProduto = it.getString("imageUrlProduto")
        }
    }

    private fun loadProductData() {
        // 🛑 REMOVEMOS A CONSULTA AO BANCO AQUI! Usamos apenas os argumentos.

        // Preencher Views com os dados recebidos
        binding.txtNomeProduto.text = nomeProduto ?: "Produto"
        binding.txtDescricaoProduto.text = descricaoProduto

        // Carregar Imagem
        imageUrlProduto?.let { url ->
            if (url.isNotEmpty()) {
                // Certifique-se de que o Glide ou a biblioteca de sua escolha está importada
                Glide.with(this)
                    .load(url)
                    .centerCrop()
                    // .placeholder(R.drawable.placeholder_produto) // Adicione um placeholder
                    .into(binding.imgProduto)
            }
        }

        // O preço unitário já está em 'precoUnitario'

        // Atualiza o display inicial de preço e quantidade
        updateQuantityDisplay()
    }

    private fun initListeners() {
        // Botão Voltar (topo esquerdo)
        binding.btnVoltar.setOnClickListener {
            // Volta para a tela anterior (CardapioFragment)
            findNavController().popBackStack()
        }

        // Botão Aumentar Quantidade (+)
        binding.btnAumentarQtd.setOnClickListener {
            quantidade++
            updateQuantityDisplay()
        }

        // Botão Diminuir Quantidade (-)
        binding.btnDiminuirQtd.setOnClickListener {
            if (quantidade > 1) {
                quantidade--
                updateQuantityDisplay()
            }
        }

        // Botão Adicionar ao Carrinho (laranja)
        binding.btnAdicionarCarrinho.setOnClickListener {
            adicionarAoCarrinho()
        }
    }

    private fun updateQuantityDisplay() {
        binding.txtQuantidade.text = quantidade.toString()

        // Calcula o preço total (quantidade * preço unitário)
        val precoTotal = quantidade * precoUnitario

        // Formata o preço para exibição (R$ X,XX)
        val precoFormatado = String.format("R$ %.2f", precoTotal)
        binding.txtPreco.text = precoFormatado
    }

    // Em ProdutoFragment.kt (Apenas a função adicionarAoCarrinho)

    private fun adicionarAoCarrinho() {
        val db = com.google.firebase.Firebase.firestore
        val auth = com.google.firebase.Firebase.auth
        val userId = auth.currentUser?.uid

        // Verificação de segurança se o usuário está logado
        if (userId == null) {
            Toast.makeText(context, "Faça login para adicionar ao carrinho", Toast.LENGTH_SHORT).show()
            return
        }

        val itemProdutoId = produtoId
        val quantidadeFinal = quantidade
        val nomeFinal = nomeProduto ?: "Produto"
        val imageUrlFinal = imageUrlProduto ?: ""

        if (itemProdutoId == null) {
            Toast.makeText(context, "Erro: ID do produto não encontrado.", Toast.LENGTH_SHORT).show()
            return
        }

        val carrinhoRef = db.collection("Usuario").document(userId).collection("Carrinho")
        val docProduto = carrinhoRef.document(itemProdutoId)

        // Lógica para verificar se o produto existe no carrinho
        docProduto.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val qtdAtual = document.getLong("quantidade")?.toInt() ?: 0

                // ATUALIZAÇÃO: Se o item já existe, incrementa a quantidade
                val updates = hashMapOf<String, Any>(
                    "quantidade" to (qtdAtual + quantidadeFinal),
                    "imageUrl" to imageUrlFinal // Garante que a URL da imagem seja salva/atualizada
                )

                docProduto.update(updates as Map<String, Any>)
                    .addOnSuccessListener {
                        // ✅ SUCESSO NA ATUALIZAÇÃO
                        Toast.makeText(context, "+$quantidadeFinal de $nomeFinal. Indo para o Carrinho!", Toast.LENGTH_SHORT).show()
                        navegarParaCarrinho()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Erro ao atualizar carrinho.", Toast.LENGTH_SHORT).show()
                    }

            } else {
                // CRIAÇÃO: Se o item não existe, cria um novo
                val novoItem = com.QuickMenu.mobile.main.carrinho.ItemCarrinho(
                    produtoId = itemProdutoId,
                    nome = nomeFinal,
                    preco = precoUnitario,
                    quantidade = quantidadeFinal,
                    imageUrl = imageUrlFinal
                )

                docProduto.set(novoItem)
                    .addOnSuccessListener {
                        // ✅ SUCESSO NA CRIAÇÃO
                        Toast.makeText(context, "$nomeFinal (x$quantidadeFinal) adicionado ao carrinho! Indo para o Carrinho.", Toast.LENGTH_SHORT).show()
                        navegarParaCarrinho()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Erro ao adicionar item.", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    // 🆕 NOVA FUNÇÃO: Abstrai a navegação
    private fun navegarParaCarrinho() {
        try {
            // Assume que a ação "action_produtoFragment_to_carrinhoFragment" existe no seu nav_graph
            findNavController().navigate(R.id.action_produtoFragment_to_carrinhoFragment)
        } catch (e: Exception) {
            Log.e("ProdutoFragment", "Erro ao navegar para o Carrinho: ${e.message}")
            Toast.makeText(context, "Erro de navegação interna.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
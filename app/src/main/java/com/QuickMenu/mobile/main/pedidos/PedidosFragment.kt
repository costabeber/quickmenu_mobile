package com.QuickMenu.mobile.main.pedidos

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.QuickMenu.mobile.databinding.FragmentPedidosBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

class PedidosFragment : Fragment() {

    private var _binding: FragmentPedidosBinding? = null
    private val binding get() = _binding!!

    private val db = Firebase.firestore
    private val auth = Firebase.auth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPedidosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        fetchOrders()
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        // Inicializa com lista vazia para evitar erros visuais antes do carregamento
        binding.recyclerView.adapter = PedidosAdapter(emptyList())
    }

    private fun fetchOrders() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(context, "Usuário não logado", Toast.LENGTH_SHORT).show()
            return
        }

        // Usamos lifecycleScope para fazer operações assíncronas de forma limpa
        lifecycleScope.launch {
            try {
                // 1. Busca os documentos principais dos pedidos
                // Ordenado por 'dataPedido' (criado no carrinho) descrescente (mais recente primeiro)
                val pedidosSnapshot = db.collection("Usuario")
                    .document(userId)
                    .collection("Pedidos")
                    .orderBy("dataPedido", Query.Direction.DESCENDING)
                    .limit(5)
                    .get()
                    .await() // await() suspende a execução até o resultado chegar

                val listaPedidosMontada = mutableListOf<Pedido>()

                // 2. Para cada pedido encontrado, buscamos seus itens e formatamos
                for (document in pedidosSnapshot.documents) {
                    val pedidoId = document.id
                    val dados = document.data

                    val idRestaurante = dados?.get("idRestaurante") as? String ?: "Restaurante Exemplo"
                    val precoTotal = dados?.get("precoTotal") as? Double ?: 0.0

                    // Converte o ID (yyyyMMdd_HHmmss) para horário legível
                    val horarioFormatado = converterIdParaHorario(pedidoId)

                    // 3. Busca a SUBCOLEÇÃO "Itens" deste pedido específico
                    val itensSnapshot = document.reference.collection("Itens").get().await()

                    // Converte os documentos da subcoleção para objetos ProdutoPedido
                    val produtosList = itensSnapshot.documents.mapNotNull { itemDoc ->
                        itemDoc.toObject(ProdutoPedido::class.java)
                    }

                    // Cria o objeto final
                    val novoPedido = Pedido(
                        id = pedidoId,
                        restauranteId = idRestaurante,
                        produtoPedidos = produtosList,
                        precoTotal = precoTotal,
                        status = Status.Ativo, // Status hardcoded por enquanto
                        horarioFormatado = horarioFormatado
                    )

                    listaPedidosMontada.add(novoPedido)
                }

                // 4. Atualiza a UI na Thread principal
                if (listaPedidosMontada.isEmpty()) {
                    Toast.makeText(context, "Nenhum pedido recente.", Toast.LENGTH_SHORT).show()
                } else {
                    binding.recyclerView.adapter = PedidosAdapter(listaPedidosMontada)
                }

            } catch (e: Exception) {
                Log.e("PedidosFragment", "Erro ao buscar pedidos", e)
                Toast.makeText(context, "Erro ao carregar pedidos.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Função auxiliar para formatar a data a partir do ID
    private fun converterIdParaHorario(pedidoId: String): String {
        return try {
            // O formato do ID gerado no Carrinho é: yyyyMMdd_HHmmss
            val formatoEntrada = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            val data = formatoEntrada.parse(pedidoId)

            // O formato de saída desejado (ex: 14:30 ou 23/11 14:30)
            // Você pediu horário, vou colocar HH:mm
            val formatoSaida = SimpleDateFormat("HH:mm - dd/MM", Locale("pt", "BR"))

            if (data != null) formatoSaida.format(data) else "--:--"
        } catch (e: Exception) {
            Log.e("DataParse", "Erro ao converter data do ID: $pedidoId", e)
            pedidoId // Retorna o próprio ID se falhar
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
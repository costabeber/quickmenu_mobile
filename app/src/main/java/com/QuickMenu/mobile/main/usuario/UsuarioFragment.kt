package com.QuickMenu.mobile.main.usuario

import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.QuickMenu.mobile.databinding.FragmentUsuarioBinding
import com.QuickMenu.mobile.main.MainActivity
import com.QuickMenu.mobile.main.home.ItemRestaurante
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class UsuarioFragment : Fragment() {

    private var _binding: FragmentUsuarioBinding? = null
    private val binding get() = _binding!!
    private lateinit var banco: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    // Variáveis para a lógica dos restaurantes
    private var allRestaurants = mutableListOf<ItemRestaurante>()
    private var favoriteRestaurantIds = setOf<String>()
    private var lastSelectedRestaurantIds = listOf<String>()


    // Variável para guardar a URL atual para visualização
    private var currentPhotoUrl: String? = null

    // Contrato para abrir a galeria
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            uploadImageToImageBB(uri)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentUsuarioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        banco = Firebase.firestore
        auth = Firebase.auth

        initListener()
        loadUserData()
        loadAndDisplayPriorityRestaurants()
    }

    private fun initListener() {
        binding.voltar.setOnClickListener { logout() }

        binding.fotoPerfil.setOnClickListener {
            showOptionsDialog()
        }
    }

    //POP-UP
    private fun showOptionsDialog() {
        val options = arrayOf("Visualizar foto", "Alterar foto")

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Foto de Perfil")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> showFullImage() // Visualizar
                1 -> pickImageLauncher.launch("image/*") // Alterar
            }
        }
        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }

    //VISUALIZAR IMAGEM
    private fun showFullImage() {
        if (currentPhotoUrl.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Nenhuma foto para visualizar", Toast.LENGTH_SHORT).show()
            return
        }

        // 1. Criar um container (LinearLayout) para dar margem e garantir o layout
        val container = android.widget.LinearLayout(requireContext())
        container.orientation = android.widget.LinearLayout.VERTICAL
        val params = android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(50, 50, 50, 50) // Margens laterais
        container.layoutParams = params

        // 2. Criar a ImageView
        val imageView = ImageView(requireContext())

        // Define que a imagem deve preencher a largura, mas ajustar a altura
        imageView.layoutParams = android.view.ViewGroup.LayoutParams(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            1000 // Altura fixa inicial grande ou WRAP_CONTENT com minHeight
        )

        // Configurações visuais
        imageView.scaleType = ImageView.ScaleType.FIT_CENTER
        imageView.adjustViewBounds = true

        // Adiciona a imagem ao container
        container.addView(imageView)

        //Carregar com Glide
        Glide.with(this)
            .load(currentPhotoUrl)
            .placeholder(com.QuickMenu.mobile.R.drawable.default_profile_picture) // Mostra algo enquanto carrega
            .into(imageView)

        // Exibir o Dialog com o container
        AlertDialog.Builder(requireContext())
            .setTitle("Foto de Perfil")
            .setView(container) // Passa o container, não a imagem direta
            .setPositiveButton("Fechar", null)
            .show()
    }

    // --- UPLOAD PARA IMAGE BB ---
    private fun uploadImageToImageBB(imageUri: Uri) {
        Toast.makeText(requireContext(), "Fazendo upload...", Toast.LENGTH_SHORT).show()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val apiKey = "3b1fc0436f09d45aab3d2388edf3099e"
                val client = OkHttpClient()

                val inputStream = requireContext().contentResolver.openInputStream(imageUri)
                val bytes = inputStream?.readBytes()
                inputStream?.close()

                if (bytes == null) throw Exception("Erro ao ler arquivo")

                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("key", apiKey)
                    .addFormDataPart("image", "profile.jpg", bytes.toRequestBody("image/jpeg".toMediaTypeOrNull()))
                    .build()

                val request = Request.Builder()
                    .url("https://api.imgbb.com/1/upload")
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                val responseString = response.body?.string()

                if (response.isSuccessful && responseString != null) {
                    val json = JSONObject(responseString)
                    val newUrl = json.getJSONObject("data").getString("url")

                    // Voltar para a thread principal para salvar no banco
                    withContext(Dispatchers.Main) {
                        updateFirestore(newUrl)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Erro no servidor de imagem", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Erro: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // ATUALIZAR FIRESTORE
    private fun updateFirestore(url: String) {
        val uid = auth.currentUser?.uid ?: return

        banco.collection("Usuario").document(uid)
            .update("profileImageUrl", url)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Foto atualizada com sucesso!", Toast.LENGTH_SHORT).show()

                // Atualiza a interface com a nova URL
                loadProfileImage(url)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Erro ao salvar no banco", Toast.LENGTH_SHORT).show()
            }
    }

    // CARREGAR DADOS DO BANCO
    private fun loadUserData() {
        val uid = auth.currentUser?.uid ?: return

        // Pega dados simples do Auth
        binding.email.text = auth.currentUser?.email

        //Pega dados do Firestore
        banco.collection("Usuario").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val username = document.getString("username")
                    val photoUrl = document.getString("profileImageUrl")

                    binding.nome.text = username ?: "Sem nome"

                    // Se tiver URL no banco, carrega. Se não, usa imagem padrão
                    if (!photoUrl.isNullOrEmpty()) {
                        loadProfileImage(photoUrl)
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Erro ao buscar dados", Toast.LENGTH_SHORT).show()
            }
    }

    // Função auxiliar para carregar a imagem na tela usando Glide
    private fun loadProfileImage(url: String) {
        currentPhotoUrl = url // Atualiza a variável global para o "Visualizar" usar

        try {
            Glide.with(this)
                .load(url)
                .circleCrop() // Corta em círculo
                .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache inteligente
                .placeholder(com.QuickMenu.mobile.R.drawable.default_profile_picture) // Enquanto carrega
                .error(com.QuickMenu.mobile.R.drawable.default_profile_picture) // Se der erro
                .into(binding.fotoPerfil)
        } catch (e: Exception) {
            // Evita crash se a tela já tiver fechado
        }
    }

    private fun logout() {
        auth.signOut()
        parentFragmentManager.popBackStack()
        (requireActivity() as MainActivity).navigateToAuth()
    }

    private fun loadAndDisplayPriorityRestaurants() {
        // Carrega as preferências do usuário (favoritos e últimos vistos)
        loadUserPreferences()

        // Busca todos os restaurantes no Firestore
        banco.collectionGroup("restaurantes").get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) return@addOnSuccessListener

                allRestaurants.clear()
                for (document in result) {
                    val restaurante = document.toObject(ItemRestaurante::class.java).copy(id = document.id)
                    allRestaurants.add(restaurante)
                }

                // Após carregar tudo, aplica a lógica de ordenação e exibe as imagens
                displayPriorityRestaurants()
            }
            .addOnFailureListener {
                // Lida com o erro, se necessário
            }
    }

    private fun loadUserPreferences() {
        val prefs = activity?.getSharedPreferences("RestaurantPreferences", Context.MODE_PRIVATE) ?: return
        favoriteRestaurantIds = prefs.getStringSet("favorite_ids", emptySet()) ?: emptySet()
        // Supondo que você salve os últimos selecionados como uma string de IDs separados por vírgula
        lastSelectedRestaurantIds = prefs.getString("last_selected_ids", "")?.split(",")?.filter { it.isNotEmpty() } ?: emptyList()
    }

    private fun displayPriorityRestaurants() {
        // 1. Calcula a pontuação para cada restaurante usando a HIERARQUIA CORRETA
        val scoredRestaurants = allRestaurants.map { restaurant ->
            val isFavorite = favoriteRestaurantIds.contains(restaurant.id)
            val lastSelectedIndex = lastSelectedRestaurantIds.indexOf(restaurant.id)
            val isLastSelected = lastSelectedIndex != -1

            var score = 0
            if (isFavorite) {
                // REGRA 1: SE É FAVORITO, A PONTUAÇÃO BASE É ALTA (ex: 1000)
                score = 1000

                if (isLastSelected) {
                    // BÔNUS: Se também for recente, ganha um bônus para desempate.
                    // Quanto mais recente (menor o índice), maior o bônus.
                    score += (100 - lastSelectedIndex) // Ex: 1100, 1099, 1098...
                }
            } else if (isLastSelected) {
                // REGRA 2: SE NÃO É FAVORITO, MAS É RECENTE, A PONTUAÇÃO BASE É BEM MENOR (ex: 100)
                score = 100 - lastSelectedIndex // Ex: 100, 99, 98...
            }

            Pair(restaurant, score) // Retorna o restaurante e sua pontuação final
        }
            // 2. Filtra (mantém apenas quem tem pontuação), ordena pela maior pontuação e mapeia de volta
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
            .map { it.first }

        // 3. Pega os 3 melhores (ou menos, se não houver 3)
        val topRestaurants = scoredRestaurants.take(3)

        // 4. Lista dos ImageViews do seu layout
        val imageViews = listOf(binding.ivRestaurante1, binding.ivRestaurante2, binding.ivRestaurante3)

        // Limpa o estado anterior, escondendo todas as imagens
/*        imageViews.forEach {
            it.visibility = View.INVISIBLE
            it.setOnClickListener(null) // Remove cliques antigos para evitar bugs
        }*/

        // 5. Popula os ImageViews com as imagens e define os cliques
        topRestaurants.forEachIndexed { index, restaurant ->
            if (index < imageViews.size) {
                val imageView = imageViews[index]
                imageView.visibility = View.VISIBLE // Torna a imagem visível

                Glide.with(this)
                    .load(restaurant.imageUrl)
                    .placeholder(com.QuickMenu.mobile.R.drawable.default_profile_picture)
                    .error(com.QuickMenu.mobile.R.drawable.bolo)
                    .into(imageView)

                // Opcional: Adicionar um clique para levar à tela do restaurante
                imageView.setOnClickListener {
                    Toast.makeText(context, "Clicou em ${restaurant.nome}", Toast.LENGTH_SHORT).show()
                    // TODO: Navegar para a tela de detalhes do 'restaurant'
                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
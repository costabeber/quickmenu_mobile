package com.QuickMenu.mobile.main.usuario

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.R
import androidx.navigation.fragment.findNavController
import com.QuickMenu.mobile.auth.AuthActivity
import com.QuickMenu.mobile.databinding.FragmentUsuarioBinding
import com.QuickMenu.mobile.main.MainActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class UsuarioFragment : Fragment() {

    // 1. Configuração do View Binding segura para Fragments
    private var _binding: FragmentUsuarioBinding? = null
    private val binding get() = _binding!!
    private lateinit var banco: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    //salva o nome de usuário depois de ter executado uma vez
    private var cachedUsername: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Infla o layout do usuário
        _binding = FragmentUsuarioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        banco = Firebase.firestore

        auth = Firebase.auth

        initListener()

        if (cachedUsername!=null){

            binding.nome.text = cachedUsername

        }else{

            loadUsernameString(onSuccess = { username ->
                cachedUsername = username
                binding.nome.text = username

            } , onFailure = { error ->
                println("Falha ao carregar o nome: ${error.message}")

            })
        }



        // Adicione aqui qualquer lógica de inicialização de UI específica da tela de usuário
        // Ex: Carregar dados do usuário, exibir mensagens, etc.
    }

    private fun initListener(){
        binding.voltar.setOnClickListener{
            logout()
        }
    }
    private fun logout(){

        auth.signOut()
        parentFragmentManager.popBackStack()
        (requireActivity() as MainActivity).navigateToAuth()
    }

    // Função que retorna o onSuccess ou o onFailure igual o Firebase normal
    fun loadUsernameString(onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {

        val uid = auth.currentUser?.uid

        if (uid.isNullOrEmpty()) {
            onFailure(Exception("Usuário não logado ou UID indisponível."))
            return
        }

        // Acessa o documento
        banco.collection("Usuario").document(uid)
            .get() // Busca única
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // 💡 SOLUÇÃO: Usamos getString() para ler um campo específico.
                    val username = document.getString("username")

                    if (username != null) {
                        onSuccess(username) // Retorna APENAS a String
                    } else {
                        onFailure(Exception("O campo 'username' não foi encontrado ou está nulo no documento."))
                    }
                } else {
                    onFailure(Exception("Documento do usuário não encontrado no Firestore."))
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    // 2. Limpeza essencial do binding
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
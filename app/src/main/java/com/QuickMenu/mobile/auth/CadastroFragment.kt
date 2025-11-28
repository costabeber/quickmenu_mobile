package com.QuickMenu.mobile.auth

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment // Alterado de AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.QuickMenu.mobile.R

import com.QuickMenu.mobile.databinding.FragmentCadastroBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class CadastroFragment : Fragment() {

    private var _binding: FragmentCadastroBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var banco: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCadastroBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = Firebase.auth
        banco = Firebase.firestore

        initListener()
    }

    private fun initListener(){
        binding.btnCadastrarUsuario.setOnClickListener {
            validateData()
        }
    }

    private fun validateData(){

        val nomeUsuario = binding.etNomeUsuario.text.toString().trim()
        val email = binding.etEmailCadastro.text.toString().trim()
        val senha = binding.etSenhaCadastro.text.toString().trim()

        if (nomeUsuario.isEmpty() || email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(requireContext(), "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show()
            return

        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(requireContext(), "Por favor, insira um e-mail válido.", Toast.LENGTH_SHORT).show()
            return

        } else if (senha.length < 6) {
            Toast.makeText(requireContext(), "A senha deve ter pelo menos 6 caracteres.", Toast.LENGTH_SHORT).show()
            return

        } else {
            registerUser(email,senha)
        }

    }

    private fun registerUser(email: String, password: String){
        try {
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->

                if (task.isSuccessful) {
                    saveUserData()
                    findNavController().navigate(R.id.action_cadastroFragment_to_loginFragment)

                } else {
                    binding.progressBar.isVisible = false
                    Toast.makeText(
                        requireContext(),
                        task.exception?.message,
                        Toast.LENGTH_SHORT,
                        ).show()

                    }
                }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), e.message.toString(), Toast.LENGTH_SHORT).show()
        }

    }

    private fun saveUserData(){

        val uid = auth.currentUser?.uid
        val username = binding.etNomeUsuario.text.toString()

        val userMap = hashMapOf(
            "username" to username,
            "email" to auth.currentUser?.email,
            "profileImageUrl" to "https://ibb.co/d426DjQJ", // Salva o link da foto padrão
        )

        banco.collection("Usuario")
            .document(uid.toString())
            .set(userMap) // Cria o documento com os dados iniciais
            .addOnSuccessListener {
                println("Perfil de usuário salvo com sucesso no Firestore: $uid")
            }
            .addOnFailureListener { e ->
                println("Falha ao salvar o perfil no Firestore: ${e.message}")
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
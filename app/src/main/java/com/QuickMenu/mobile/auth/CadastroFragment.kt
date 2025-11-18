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

class CadastroFragment : Fragment() { // Herdar de Fragment

    private var _binding: FragmentCadastroBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    // 1. Inflar o layout (substitui onCreate e setContentView)
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

            // NOTA: Em Fragments, use 'requireContext()' ou 'activity' para o Context
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
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->

                    if (task.isSuccessful) {
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
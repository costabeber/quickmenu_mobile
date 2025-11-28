package com.QuickMenu.mobile.auth

import android.content.ContentValues.TAG
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.QuickMenu.mobile.R
import com.QuickMenu.mobile.databinding.FragmentLoginBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initListeners()

        linkCadastro()
    }

    private fun initListeners(){
        binding.btnEntrar.setOnClickListener {
            validateData()
        }
    }
    private fun validateData(){

        val email = binding.etEmail.text.toString().trim()
        val senha = binding.etSenha.text.toString().trim()

        if (email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(requireContext(),
                "Preencha todos os campos",
                Toast.LENGTH_SHORT).show()

        } else {
            binding.progressBar.isVisible = true
            login(email,senha)

        }
    }

    private fun login(email: String, senha: String){
        try {
            auth = Firebase.auth

            auth.signInWithEmailAndPassword(email, senha).addOnCompleteListener { task ->

                if (task.isSuccessful){
                    Toast.makeText(
                        requireContext(),
                        "Login realizado com sucesso!",
                        Toast.LENGTH_SHORT
                    ).show()

                    (requireActivity() as AuthActivity).navigateToMain()
                } else {
                    Toast.makeText(
                        requireContext(),
                        task.exception.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.progressBar.isVisible = false
                }
            }
        }

        catch (erro : Exception){
            Toast.makeText(requireContext(),
                erro.message,
                Toast.LENGTH_SHORT).show()
        }
    }

    private fun linkCadastro(){
        val fullText = "Preencha com seus dados para realizar o cadastro"
        val spannableString = SpannableString(fullText)

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                findNavController().navigate(R.id.action_loginFragment_to_cadastroFragment)
            }
        }

        val startIndex = fullText.indexOf("cadastro")
        if (startIndex != -1) {
            val endIndex = startIndex + "cadastro".length
            spannableString.setSpan(clickableSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        binding.tvCadastro.text = spannableString
        binding.tvCadastro.movementMethod = LinkMovementMethod.getInstance()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

package com.QuickMenu.mobile.main.usuario

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.QuickMenu.mobile.databinding.FragmentUsuarioBinding

class UsuarioFragment : Fragment() {

    // 1. Configuração do View Binding segura para Fragments
    private var _binding: FragmentUsuarioBinding? = null
    private val binding get() = _binding!!

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

        // Exemplo: Se houver um botão de 'Sair' ou 'Logout' no layout do usuário
        /*
        binding.btnSair.setOnClickListener {
            // Se o usuário sair, voltamos para a tela de Login
            parentFragmentManager.popBackStack()
        }
        */

        // Adicione aqui qualquer lógica de inicialização de UI específica da tela de usuário
        // Ex: Carregar dados do usuário, exibir mensagens, etc.
    }

    // 2. Limpeza essencial do binding
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
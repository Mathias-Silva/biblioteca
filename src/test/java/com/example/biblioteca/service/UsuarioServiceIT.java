package com.example.biblioteca.service;

import com.example.biblioteca.AbstractIntegrationTest;
import com.example.biblioteca.model.Usuario;
import com.example.biblioteca.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioServiceIT extends AbstractIntegrationTest {

    @Autowired
    private UsuarioService usuarioService; // Serviço responsável por cadastrar usuários

    @Autowired
    private UsuarioRepository usuarioRepository; // Repositório para validar persistência no MongoDB

    @BeforeEach
    void setUp() {
        usuarioRepository.deleteAll(); // Limpa o banco antes de cada teste
    }

    @Test
    @DisplayName("Deve cadastrar um novo usuário no MongoDB real e criptografar a senha")
    void devePersistirUsuarioCriado() {
        // 1. Cenário: cria um usuário com dados simples
        Usuario novoUsuario = new Usuario();
        novoUsuario.setNome("Mathias");
        novoUsuario.setEmail("mathias@email.com");
        novoUsuario.setSenha("senha123"); // senha em texto puro

        // 2. Ação: chama o serviço para cadastrar o usuário
        var usuarioSalvo = usuarioService.cadastrar(novoUsuario);

        // 3. Validação: o MongoDB deve ter gerado um ID
        assertNotNull(usuarioSalvo.getId(), "O MongoDB deveria ter gerado um ID");

        // Busca o usuário no banco e valida os dados
        var usuarioNoBanco = usuarioRepository.findById(usuarioSalvo.getId());
        assertTrue(usuarioNoBanco.isPresent());
        assertEquals("mathias@email.com", usuarioNoBanco.get().getEmail());

        // 4. Segurança: garante que a senha não foi salva em texto puro
        assertNotEquals("senha123", usuarioNoBanco.get().getSenha());
    }
}

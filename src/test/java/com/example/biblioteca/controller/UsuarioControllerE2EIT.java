package com.example.biblioteca.controller;

import com.example.biblioteca.AbstractIntegrationTest;
import com.example.biblioteca.model.Usuario;
import com.example.biblioteca.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UsuarioControllerE2EIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc; // Simula requisições HTTP para os controllers

    @Autowired
    private UsuarioRepository usuarioRepository; // Acesso direto ao MongoDB para validar persistência

    @BeforeEach
    void setUp() {
        usuarioRepository.deleteAll(); // Limpa o banco antes de cada teste
    }

    @Test
    @DisplayName("Deve acessar a tela de cadastro livremente")
    void deveAcessarTelaCadastro() throws Exception {
        mockMvc.perform(get("/usuarios/cadastro")) // Requisição GET para a tela de cadastro
                .andExpect(status().isOk()) // Deve retornar 200 OK
                .andExpect(view().name("cadastro")); // Renderiza a view "cadastro"
    }

    @Test
    @DisplayName("Deve submeter o formulário de cadastro, salvar no MongoDB real e redirecionar para o login")
    void deveCadastrarUsuarioViaFormulario() throws Exception {
        usuarioRepository.deleteAll(); // Garante que não exista usuário prévio

        // Simula envio de formulário HTML para cadastro de usuário
        mockMvc.perform(post("/usuarios/cadastro")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED) // Tipo de envio do form
                        .param("nome", "Mathias Teste")
                        .param("email", "mathias.e2e@email.com")
                        .param("senha", "minhasenha123"))
                .andExpect(status().is3xxRedirection()) // Deve redirecionar após salvar
                .andExpect(redirectedUrl("/login?sucesso")); // Redireciona para login com flag de sucesso

        // Valida se o usuário foi realmente persistido no banco
        Optional<Usuario> usuarioSalvo = usuarioRepository.findByEmail("mathias.e2e@email.com");
        assertTrue(usuarioSalvo.isPresent(), "O usuário deveria ter sido salvo no banco real");
        assertEquals("Mathias Teste", usuarioSalvo.get().getNome());
        assertNotEquals("minhasenha123", usuarioSalvo.get().getSenha(),
                "A senha deve estar criptografada no banco");
    }
}

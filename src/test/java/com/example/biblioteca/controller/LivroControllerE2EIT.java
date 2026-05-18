package com.example.biblioteca.controller;

import com.example.biblioteca.model.Livro;
import com.example.biblioteca.repository.LivroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.biblioteca.AbstractIntegrationTest;

class LivroControllerE2EIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc; // Simula requisições HTTP para os controllers

    @Autowired
    private LivroRepository livroRepository; // Acesso direto ao MongoDB para validar persistência

    @BeforeEach
    void setUp() {
        livroRepository.deleteAll(); // Limpa o banco antes de cada teste
    }

    @Test
    @DisplayName("Deve bloquear acesso anônimo à listagem de livros e redirecionar para o login")
    void deveBloquearAcessoSemSessao() throws Exception {
        mockMvc.perform(get("/livros")) // Requisição sem usuário logado
                .andExpect(status().is3xxRedirection()) // Deve redirecionar
                .andExpect(redirectedUrlPattern("**/login")); // Para a tela de login
    }

    @Test
    @DisplayName("Deve permitir acesso à listagem se o usuário possuir sessão ativa")
    void devePermitirAcessoComSessao() throws Exception {
        mockMvc.perform(get("/livros")
                        .with(user(User.withUsername("teste@email.com")
                                .password("senha")
                                .roles("USER")
                                .build()))) // Simula usuário autenticado
                .andExpect(status().isOk()); // Deve retornar 200 OK
    }

    @Test
    @DisplayName("Deve salvar livro via requisição POST atrelado ao e-mail da sessão ativa")
    void deveSalvarLivroViaController() throws Exception {
        String emailSessao = "bibliotecario@email.com";

        mockMvc.perform(post("/livros/salvar")
                        .with(user(emailSessao)) // Simula usuário logado
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED) // Tipo de envio do form
                        .param("titulo", "Arquitetura Limpa")
                        .param("autor", "Robert C. Martin")
                        .param("isbn", "9788550804606")
                        .param("genero", "Tecnologia"))
                .andExpect(status().is3xxRedirection()) // Deve redirecionar após salvar
                .andExpect(redirectedUrl("/livros"));

        // Valida se o livro foi realmente persistido no banco
        List<Livro> livrosDoBanco = livroRepository.findByUsuarioId(emailSessao);
        assertEquals(1, livrosDoBanco.size());
        assertEquals("Arquitetura Limpa", livrosDoBanco.get(0).getTitulo());
        assertEquals(emailSessao, livrosDoBanco.get(0).getUsuarioId(),
                "O ID do usuário deve ser o e-mail extraído da sessão");
    }

    @Test
    @DisplayName("Deve excluir um livro existente do banco através da rota POST de exclusão")
    void deveExcluirLivro() throws Exception {
        // Cria um livro manualmente para depois excluir
        Livro livroParaExcluir = Livro.builder()
                .titulo("Livro Fantasma")
                .autor("Desconhecido")
                .usuarioId("teste@email.com")
                .build();
        livroParaExcluir = livroRepository.save(livroParaExcluir);

        mockMvc.perform(post("/livros/excluir/" + livroParaExcluir.getId())
                        .with(user("teste@email.com"))) // Usuário autenticado
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/livros"));

        // Confirma que o livro foi removido do banco
        assertFalse(livroRepository.findById(livroParaExcluir.getId()).isPresent());
    }
}

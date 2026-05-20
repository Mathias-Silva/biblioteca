package com.example.biblioteca.controller;

import com.example.biblioteca.AbstractIntegrationTest;
import com.example.biblioteca.model.Livro;
import com.example.biblioteca.repository.LivroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("LivroController - Testes de integração sem mocks")
class LivroControllerTest extends AbstractIntegrationTest {

    private static final String EMAIL_USUARIO = "user@email.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LivroRepository livroRepository;

    @BeforeEach
    void setUp() {
        // Limpa a coleção de livros antes de cada teste (Testcontainers MongoDB)
        livroRepository.deleteAll();
    }

    @Test
    @DisplayName("deveExibirListaDeLivros")
    void deveExibirListaDeLivros() throws Exception {
        livroRepository.save(Livro.builder()
                .titulo("Meu Livro")
                .autor("Autor")
                .usuarioId(EMAIL_USUARIO)
                .build());

        // Testa renderização da view 'livros' para usuário autenticado
        mockMvc.perform(get("/livros")
                        .with(user(EMAIL_USUARIO)))
                .andExpect(status().isOk())
                .andExpect(view().name("livros"))
                .andExpect(model().attributeExists("livros"));
    }

    @Test
    @DisplayName("deveBuscarIsbnViaApi")
    void deveBuscarIsbnViaApi() throws Exception {
        // Teste que chama o endpoint que delega para Google Books (WireMock fornece a resposta)
        mockMvc.perform(get("/livros/buscar-isbn")
                        .param("isbn", "9788576082675")
                        .with(user(EMAIL_USUARIO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.encontrado").value(true))
                .andExpect(jsonPath("$.titulo").value("Código Limpo"));
    }

    @Test
    @DisplayName("deveExibirTelaDeNovoLivro")
    void deveExibirTelaDeNovoLivro() throws Exception {
        mockMvc.perform(get("/livros/novo").with(user(EMAIL_USUARIO)))
                .andExpect(status().isOk())
                .andExpect(view().name("cadastro-livro"))
                .andExpect(model().attributeExists("livro"));
    }

    @Test
    @DisplayName("deveSalvarNovoLivroERedirecionar")
    void deveSalvarNovoLivroERedirecionar() throws Exception {
        // Submete formulário com CSRF (proteção) — ação que grava no banco
        mockMvc.perform(post("/livros/salvar")
                        .with(user(EMAIL_USUARIO))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("titulo", "Test-driven Development")
                        .param("autor", "Kent Beck")
                        .param("isbn", "123456789")
                        .param("genero", "Tecnologia")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/livros"));
    }

}

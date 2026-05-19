package com.example.biblioteca.controller;

import com.example.biblioteca.AbstractIntegrationTest;
import com.example.biblioteca.model.Livro;
import com.example.biblioteca.repository.LivroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

        mockMvc.perform(get("/livros")
                        .with(user(EMAIL_USUARIO)))
                .andExpect(status().isOk())
                .andExpect(view().name("livros"))
                .andExpect(model().attributeExists("livros"));
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

    @Test
    @DisplayName("deveExcluirLivroERedirecionar")
    void deveExcluirLivroERedirecionar() throws Exception {
        Livro livro = livroRepository.save(Livro.builder()
                .titulo("Excluir")
                .autor("Autor")
                .usuarioId(EMAIL_USUARIO)
                .build());

        mockMvc.perform(post("/livros/excluir/" + livro.getId())
                        .with(user(EMAIL_USUARIO))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/livros"));
    }

    @Test
    @DisplayName("deveExibirTelaDeEdicaoSeLivroExistir")
    void deveExibirTelaDeEdicaoSeLivroExistir() throws Exception {
        Livro livro = livroRepository.save(Livro.builder()
                .titulo("Livro Teste")
                .autor("Autor")
                .usuarioId(EMAIL_USUARIO)
                .build());

        mockMvc.perform(get("/livros/editar/" + livro.getId()).with(user(EMAIL_USUARIO)))
                .andExpect(status().isOk())
                .andExpect(view().name("editar-livro"))
                .andExpect(model().attributeExists("livro"));
    }

    @Test
    @DisplayName("deveAtualizarLivroERedirecionar")
    void deveAtualizarLivroERedirecionar() throws Exception {
        Livro livro = livroRepository.save(Livro.builder()
                .titulo("Original")
                .autor("Autor")
                .isbn("123")
                .genero("Educação")
                .usuarioId(EMAIL_USUARIO)
                .build());

        mockMvc.perform(post("/livros/editar/" + livro.getId())
                        .with(user(EMAIL_USUARIO))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("titulo", "Título Atualizado")
                        .param("autor", "Autor")
                        .param("isbn", "123")
                        .param("genero", "Educação")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/livros"));
    }

    @Test
    @DisplayName("deveBloquearPostSemCsrf")
    void deveBloquearPostSemCsrf() throws Exception {
        mockMvc.perform(post("/livros/salvar")
                        .with(user(EMAIL_USUARIO))
                        .param("titulo", "Livro")
                        .param("autor", "Autor")
                        .param("isbn", "isbn")
                        .param("genero", "Gênero"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("deveAceitarPostComCsrfValido")
    void deveAceitarPostComCsrfValido() throws Exception {
        mockMvc.perform(post("/livros/salvar")
                        .with(user(EMAIL_USUARIO))
                        .param("titulo", "Livro Seguro")
                        .param("autor", "Autor")
                        .param("isbn", "isbn-seguro")
                        .param("genero", "Gênero")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/livros"));
    }
}

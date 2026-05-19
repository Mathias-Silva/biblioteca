package com.example.biblioteca.controller;

import com.example.biblioteca.dto.LivroRequestDTO;
import com.example.biblioteca.model.Livro;
import com.example.biblioteca.repository.LivroRepository;
import com.example.biblioteca.service.LivroService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Optional;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LivroController.class)
class LivroControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LivroService livroService;

    @MockBean
    private LivroRepository livroRepository;

    @Test
    @WithMockUser(username = "user@email.com")
    void deveExibirListaDeLivros() throws Exception {
        when(livroService.listarPorUsuario("user@email.com")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/livros"))
                .andExpect(status().isOk())
                .andExpect(view().name("livros"))
                .andExpect(model().attributeExists("livros"));
    }

    @Test
    @WithMockUser
    void deveExibirTelaDeNovoLivro() throws Exception {
        mockMvc.perform(get("/livros/novo"))
                .andExpect(status().isOk())
                .andExpect(view().name("cadastro-livro"))
                .andExpect(model().attributeExists("livro"));
    }

    @Test
    @WithMockUser(username = "user@email.com")
    void deveSalvarNovoLivroERedirecionar() throws Exception {
        mockMvc.perform(post("/livros/salvar")
                        .param("titulo", "Test-driven Development")
                        .param("autor", "Kent Beck")
                        .param("isbn", "123456789")
                        .param("genero", "Tecnologia")
                        .with(csrf())) // Necessário por causa do Spring Security
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/livros"));
    }

    @Test
    @WithMockUser
    void deveExcluirLivroERedirecionar() throws Exception {
        mockMvc.perform(post("/livros/excluir/1").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/livros"));
    }

    @Test
    @WithMockUser
    void deveExibirTelaDeEdicaoSeLivroExistir() throws Exception {
        Livro livro = Livro.builder().id("1").titulo("Livro Teste").build();
        when(livroRepository.findById("1")).thenReturn(Optional.of(livro));

        mockMvc.perform(get("/livros/editar/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("editar-livro"))
                .andExpect(model().attributeExists("livro"));
    }

    @Test
    @WithMockUser(username = "user@email.com")
    void deveAtualizarLivroERedirecionar() throws Exception {
        mockMvc.perform(post("/livros/editar/1")
                        .param("titulo", "Título Atualizado")
                        .param("autor", "Autor")
                        .param("isbn", "123")
                        .param("genero", "Educação")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/livros"));
    }
}

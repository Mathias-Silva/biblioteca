package com.example.biblioteca.controller;

import com.example.biblioteca.AbstractIntegrationTest;
import com.example.biblioteca.model.Livro;
import com.example.biblioteca.model.Usuario;
import com.example.biblioteca.repository.LivroRepository;
import com.example.biblioteca.repository.UsuarioRepository;
import com.example.biblioteca.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Autenticação e Segurança - E2E")
class LoginSecurityIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private LivroRepository livroRepository;

    @Autowired
    private UsuarioService usuarioService;

    @BeforeEach
    void setUp() {
        livroRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    @Test
    @DisplayName("Login com credenciais corretas redireciona para /livros")
    void deveAutenticarUsuarioComCredenciaisCorretas() throws Exception {
        usuarioService.cadastrar(Usuario.builder()
                .nome("Teste Login")
                .email("login@email.com")
                .senha("SenhaCorreta123!")
                .build());

        mockMvc.perform(post("/login")
                        .param("username", "login@email.com")
                        .param("password", "SenhaCorreta123!")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/livros"));
    }

    @Test
    @DisplayName("Login com credenciais inválidas retorna erro")
    void deveFalharLoginComCredenciaisInvalidas() throws Exception {
        usuarioService.cadastrar(Usuario.builder()
                .nome("Teste")
                .email("login@email.com")
                .senha("SenhaCorreta123!")
                .build());

        mockMvc.perform(post("/login")
                        .param("username", "login@email.com")
                        .param("password", "SenhaErrada!")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    @DisplayName("POST sem token CSRF é bloqueado")
    void deveBloquearPostSemCsrf() throws Exception {
        mockMvc.perform(post("/usuarios/cadastro")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("nome", "Atacante")
                        .param("email", "atacante@email.com")
                        .param("senha", "Senha123!"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Usuário B não pode editar livro do Usuário A")
    void usuarioBNaoPodeEditarLivroDoUsuarioA() throws Exception {
        Livro livroA = livroRepository.save(Livro.builder()
                .titulo("Livro do A")
                .autor("Autor")
                .usuarioId("usera@email.com")
                .build());

        mockMvc.perform(get("/livros/editar/" + livroA.getId())
                        .with(user(User.withUsername("userb@email.com").password("x").roles("USER").build())))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Usuário B não pode excluir livro do Usuário A")
    void usuarioBNaoPodeExcluirLivroDoUsuarioA() throws Exception {
        Livro livroA = livroRepository.save(Livro.builder()
                .titulo("Livro do A")
                .autor("Autor")
                .usuarioId("usera@email.com")
                .build());

        mockMvc.perform(post("/livros/excluir/" + livroA.getId())
                        .with(user("userb@email.com"))
                        .with(csrf()))
                .andExpect(status().isForbidden());

        assertTrue(livroRepository.findById(livroA.getId()).isPresent());
    }

    @Test
    @DisplayName("Listagem exibe apenas livros do usuário autenticado")
    void listagemIsolaDadosPorUsuario() throws Exception {
        livroRepository.save(Livro.builder().titulo("Livro A").autor("A").usuarioId("a@email.com").build());
        livroRepository.save(Livro.builder().titulo("Livro B").autor("B").usuarioId("b@email.com").build());

        mockMvc.perform(get("/livros").with(user("a@email.com")))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("livros"));
    }

    @Test
    @DisplayName("Acesso anônimo a /livros redireciona para login")
    void deveRedirecionarParaLoginSemAuth() throws Exception {
        mockMvc.perform(get("/livros"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }
}

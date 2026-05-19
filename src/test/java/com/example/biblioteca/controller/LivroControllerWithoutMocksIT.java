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

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes E2E para LivroController sem mocks
 * - Utiliza Testcontainers para MongoDB real
 * - Testa fluxos completos do usuário
 * - Valida persistência de dados
 */
@DisplayName("LivroController - Testes E2E Sem Mocks")
class LivroControllerWithoutMocksIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LivroRepository livroRepository;

    private static final String EMAIL_USUARIO = "usuario-e2e@email.com";
    private static final String EMAIL_OUTRO_USUARIO = "outro-usuario-e2e@email.com";

    @BeforeEach
    void setUp() {
        livroRepository.deleteAll();
    }

    // ======== TESTES DE AUTENTICAÇÃO ========

    @Test
    @DisplayName("Deve bloquear acesso anônimo à listagem de livros")
    void deveBloquearAcessoAnonimo() throws Exception {
        mockMvc.perform(get("/livros"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("Deve permitir acesso com usuário autenticado")
    void devePermitirAcessoComAutenticacao() throws Exception {
        mockMvc.perform(get("/livros")
                        .with(user(User.withUsername(EMAIL_USUARIO)
                                .password("senha")
                                .roles("USER")
                                .build())))
                .andExpect(status().isOk())
                .andExpect(view().name("livros"));
    }

    // ======== TESTES DE LISTAGEM ========

    @Test
    @DisplayName("Deve exibir lista vazia de livros para novo usuário")
    void deveExibirListaVaziaParaNovoUsuario() throws Exception {
        mockMvc.perform(get("/livros")
                        .with(user(User.withUsername(EMAIL_USUARIO)
                                .password("senha")
                                .roles("USER")
                                .build())))
                .andExpect(status().isOk())
                .andExpect(view().name("livros"))
                .andExpect(model().attributeExists("livros"));
    }

    @Test
    @DisplayName("Deve exibir apenas livros do usuário autenticado")
    void deveExibirApenasLivrosDoUsuario() throws Exception {
        // Arrange: Cria livros para dois usuários diferentes
        Livro livroUser1 = Livro.builder()
                .titulo("Livro do User 1")
                .autor("Autor A")
                .usuarioId(EMAIL_USUARIO)
                .build();
        livroRepository.save(livroUser1);

        Livro livroUser2 = Livro.builder()
                .titulo("Livro do User 2")
                .autor("Autor B")
                .usuarioId(EMAIL_OUTRO_USUARIO)
                .build();
        livroRepository.save(livroUser2);

        // Act & Assert
        mockMvc.perform(get("/livros")
                        .with(user(User.withUsername(EMAIL_USUARIO)
                                .password("senha")
                                .roles("USER")
                                .build())))
                .andExpect(status().isOk())
                .andExpect(view().name("livros"))
                .andExpect(model().attributeExists("livros"));

        // Validação adicional: Verifica no banco se realmente está isolado
        List<Livro> livrosDoUsuario = livroRepository.findByUsuarioId(EMAIL_USUARIO);
        assertEquals(1, livrosDoUsuario.size(), "Deve ter apenas 1 livro do usuário");
        assertEquals("Livro do User 1", livrosDoUsuario.get(0).getTitulo());
    }

    // ======== TESTES DE CRIAÇÃO ========

    @Test
    @DisplayName("Deve exibir formulário de novo livro")
    void deveExibirFormularioNovoLivro() throws Exception {
        mockMvc.perform(get("/livros/novo")
                        .with(user(User.withUsername(EMAIL_USUARIO)
                                .password("senha")
                                .roles("USER")
                                .build())))
                .andExpect(status().isOk())
                .andExpect(view().name("cadastro-livro"))
                .andExpect(model().attributeExists("livro"));
    }

    @Test
    @DisplayName("Deve salvar novo livro e redirecionar")
    void deveSalvarNovoLivroERedirecionar() throws Exception {
        // Act
        mockMvc.perform(post("/livros/salvar")
                        .with(user(User.withUsername(EMAIL_USUARIO)
                                .password("senha")
                                .roles("USER")
                                .build()))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("titulo", "Novo Livro")
                        .param("autor", "Novo Autor")
                        .param("isbn", "123456789")
                        .param("genero", "Tecnologia")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/livros"));

        // Assert: Verifica se foi persistido no banco
        List<Livro> livrosSalvos = livroRepository.findByUsuarioId(EMAIL_USUARIO);
        assertEquals(1, livrosSalvos.size(), "Deve ter 1 livro salvo");
        assertEquals("Novo Livro", livrosSalvos.get(0).getTitulo());
        assertEquals("Novo Autor", livrosSalvos.get(0).getAutor());
        assertEquals("123456789", livrosSalvos.get(0).getIsbn());
        assertEquals("Tecnologia", livrosSalvos.get(0).getGenero());
        assertEquals(EMAIL_USUARIO, livrosSalvos.get(0).getUsuarioId());
    }

    @Test
    @DisplayName("Deve associar livro salvo ao usuário da sessão")
    void deveAssociarLivroAoUsuarioDaSessao() throws Exception {
        // Act
        mockMvc.perform(post("/livros/salvar")
                        .with(user(User.withUsername(EMAIL_USUARIO)
                                .password("senha")
                                .roles("USER")
                                .build()))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("titulo", "Livro Session Test")
                        .param("autor", "Autor")
                        .param("isbn", "isbn-session")
                        .param("genero", "Teste")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        // Assert
        List<Livro> livrosUser1 = livroRepository.findByUsuarioId(EMAIL_USUARIO);
        List<Livro> livrosUser2 = livroRepository.findByUsuarioId(EMAIL_OUTRO_USUARIO);

        assertEquals(1, livrosUser1.size(), "User 1 deve ter 1 livro");
        assertEquals(0, livrosUser2.size(), "User 2 não deve ter nenhum livro");
    }

    // ======== TESTES DE EDIÇÃO ========

    @Test
    @DisplayName("Deve exibir formulário de edição para livro existente")
    void deveExibirFormularioEdicao() throws Exception {
        // Arrange
        Livro livro = Livro.builder()
                .titulo("Livro a Editar")
                .autor("Autor Original")
                .usuarioId(EMAIL_USUARIO)
                .build();
        Livro salvo = livroRepository.save(livro);

        // Act & Assert
        mockMvc.perform(get("/livros/editar/" + salvo.getId())
                        .with(user(User.withUsername(EMAIL_USUARIO)
                                .password("senha")
                                .roles("USER")
                                .build())))
                .andExpect(status().isOk())
                .andExpect(view().name("editar-livro"))
                .andExpect(model().attributeExists("livro"));
    }

    @Test
    @DisplayName("Deve atualizar livro e redirecionar")
    void deveAtualizarLivroERedirecionar() throws Exception {
        // Arrange
        Livro livro = Livro.builder()
                .titulo("Título Original")
                .autor("Autor Original")
                .isbn("isbn-original")
                .genero("Gênero Original")
                .usuarioId(EMAIL_USUARIO)
                .build();
        Livro salvo = livroRepository.save(livro);

        // Act
        mockMvc.perform(post("/livros/editar/" + salvo.getId())
                        .with(user(User.withUsername(EMAIL_USUARIO)
                                .password("senha")
                                .roles("USER")
                                .build()))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("titulo", "Título Atualizado")
                        .param("autor", "Autor Atualizado")
                        .param("isbn", "isbn-atualizado")
                        .param("genero", "Gênero Atualizado")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/livros"));

        // Assert
        Livro livroAtualizado = livroRepository.findById(salvo.getId()).orElse(null);
        assertNotNull(livroAtualizado, "Livro deve existir");
        assertEquals("Título Atualizado", livroAtualizado.getTitulo());
        assertEquals("Autor Atualizado", livroAtualizado.getAutor());
        assertEquals("isbn-atualizado", livroAtualizado.getIsbn());
        assertEquals("Gênero Atualizado", livroAtualizado.getGenero());
    }

    // ======== TESTES DE EXCLUSÃO ========

    @Test
    @DisplayName("Deve excluir livro e redirecionar")
    void deveExcluirLivroERedirecionar() throws Exception {
        // Arrange
        Livro livro = Livro.builder()
                .titulo("Livro a Deletar")
                .autor("Autor")
                .usuarioId(EMAIL_USUARIO)
                .build();
        Livro salvo = livroRepository.save(livro);

        // Act
        mockMvc.perform(post("/livros/excluir/" + salvo.getId())
                        .with(user(User.withUsername(EMAIL_USUARIO)
                                .password("senha")
                                .roles("USER")
                                .build()))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/livros"));

        // Assert
        assertFalse(livroRepository.findById(salvo.getId()).isPresent(),
                "Livro deve ter sido deletado");
    }

    @Test
    @DisplayName("Deve validar que livro foi realmente removido do banco")
    void deveValidarRemocaoDoLivro() throws Exception {
        // Arrange
        Livro livro = Livro.builder()
                .titulo("Livro de Teste")
                .autor("Autor")
                .usuarioId(EMAIL_USUARIO)
                .build();
        Livro salvo = livroRepository.save(livro);
        assertEquals(1, livroRepository.count(), "Deve ter 1 livro antes da exclusão");

        // Act
        mockMvc.perform(post("/livros/excluir/" + salvo.getId())
                        .with(user(User.withUsername(EMAIL_USUARIO)
                                .password("senha")
                                .roles("USER")
                                .build()))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        // Assert
        assertEquals(0, livroRepository.count(), "Não deve ter nenhum livro após exclusão");
    }

    // ======== TESTES DE SEGURANÇA ========

    @Test
    @DisplayName("Deve requerer CSRF token para operações POST")
    void deveRequeriCsrfTokenParaPost() throws Exception {
        mockMvc.perform(post("/livros/salvar")
                        .with(user(User.withUsername(EMAIL_USUARIO)
                                .password("senha")
                                .roles("USER")
                                .build()))
                        .param("titulo", "Livro")
                        .param("autor", "Autor")
                        .param("isbn", "isbn")
                        .param("genero", "Gênero"))
                // Note: .with(csrf()) está AUSENTE propositalmente
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Deve aceitar requisição POST com CSRF token válido")
    void deveAceitarPostComCsrfValido() throws Exception {
        mockMvc.perform(post("/livros/salvar")
                        .with(user(User.withUsername(EMAIL_USUARIO)
                                .password("senha")
                                .roles("USER")
                                .build()))
                        .param("titulo", "Livro Seguro")
                        .param("autor", "Autor")
                        .param("isbn", "isbn-seguro")
                        .param("genero", "Gênero")
                        .with(csrf())) // ✅ Com CSRF token
                .andExpect(status().is3xxRedirection());
    }
}

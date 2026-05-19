package com.example.biblioteca.service;

import com.example.biblioteca.AbstractIntegrationTest;
import com.example.biblioteca.dto.LivroRequestDTO;
import com.example.biblioteca.dto.LivroResponseDTO;
import com.example.biblioteca.repository.LivroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LivroService - Testes de integração sem mocks")
class LivroServiceTest extends AbstractIntegrationTest {

    private static final String EMAIL_USUARIO = "user@email.com";

    @Autowired
    private LivroService livroService;

    @Autowired
    private LivroRepository livroRepository;

    @BeforeEach
    void setUp() {
        livroRepository.deleteAll();
    }

    @Test
    @DisplayName("deveSalvarLivroComSucesso")
    void deveSalvarLivroComSucesso() {
        LivroRequestDTO dto = new LivroRequestDTO(
                "Código Limpo", "Robert C. Martin", "9788576082675", "Computação");

        LivroResponseDTO resultado = livroService.salvar(dto, EMAIL_USUARIO);

        assertNotNull(resultado);
        assertEquals("Código Limpo", resultado.titulo());
        assertTrue(livroRepository.findById(resultado.id()).isPresent());
        assertEquals(EMAIL_USUARIO, livroRepository.findById(resultado.id()).get().getUsuarioId());
    }

    @Test
    @DisplayName("deveListarPorUsuario")
    void deveListarPorUsuario() {
        livroService.salvar(
                new LivroRequestDTO("Livro A", "Autor A", "isbn-a", "Tech"), EMAIL_USUARIO);
        livroService.salvar(
                new LivroRequestDTO("Livro B", "Autor B", "isbn-b", "Romance"), "outro@email.com");

        List<LivroResponseDTO> resultado = livroService.listarPorUsuario(EMAIL_USUARIO);

        assertEquals(1, resultado.size());
        assertEquals("Livro A", resultado.getFirst().titulo());
    }

    @Test
    @DisplayName("deveListarTodosOsLivros")
    void deveListarTodosOsLivros() {
        livroService.salvar(
                new LivroRequestDTO("Livro 1", "Autor", "isbn1", "Gênero"), EMAIL_USUARIO);
        livroService.salvar(
                new LivroRequestDTO("Livro 2", "Autor", "isbn2", "Gênero"), "outro@email.com");

        List<LivroResponseDTO> resultado = livroService.listarTodos();

        assertEquals(2, resultado.size());
    }

    @Test
    @DisplayName("deveBuscarPorIsbnComSucesso_UsandoWireMock")
    void deveBuscarPorIsbnComSucesso_UsandoWireMock() {
        var resultado = livroService.buscarPorIsbn("9788576082675");

        assertTrue(resultado.encontrado());
        assertEquals("Código Limpo", resultado.titulo());
        assertEquals("Robert C. Martin", resultado.autor());
        assertEquals("Computação", resultado.genero());
    }

    @Test
    @DisplayName("deveBuscarPorIsbnSemResultado")
    void deveBuscarPorIsbnSemResultado() {
        var resultado = livroService.buscarPorIsbn("0000000000000");

        assertFalse(resultado.encontrado());
    }

    @Test
    @DisplayName("usuariosNaoDevemVerLivrosUnsDosOutros")
    void usuariosNaoDevemVerLivrosUnsDosOutros() {
        livroService.salvar(new LivroRequestDTO("Livro User 1", "Autor", "isbn1", "Gênero"), "user1@email.com");
        livroService.salvar(new LivroRequestDTO("Livro User 2", "Autor", "isbn2", "Gênero"), "user2@email.com");

        List<LivroResponseDTO> livrosUser1 = livroService.listarPorUsuario("user1@email.com");
        List<LivroResponseDTO> livrosUser2 = livroService.listarPorUsuario("user2@email.com");

        assertEquals(1, livrosUser1.size());
        assertEquals(1, livrosUser2.size());
        assertTrue(livrosUser1.stream().noneMatch(l -> l.titulo().contains("User 2")));
        assertTrue(livrosUser2.stream().noneMatch(l -> l.titulo().contains("User 1")));
    }
}

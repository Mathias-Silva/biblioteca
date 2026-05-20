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
        // Limpa a base (Testcontainers MongoDB garante isolamento)
        livroRepository.deleteAll();
    }

    @Test
    @DisplayName("deveSalvarLivroComSucesso")
    void deveSalvarLivroComSucesso() {
        LivroRequestDTO dto = new LivroRequestDTO(
                "Código Limpo", "Robert C. Martin", "9788576082675", "Computação");

        // Persistência real: testa integração com MongoDB via Testcontainers
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
        // WireMock simula resposta da Google Books para ISBN conhecido
        var resultado = livroService.buscarInformacoesExternas("9788576082675");

        assertTrue(resultado.encontrado());
        assertEquals("Código Limpo", resultado.titulo());
        assertEquals("Robert C. Martin", resultado.autor());
        assertEquals("Computação", resultado.genero());
    }

    @Test
    @DisplayName("deveBuscarPorIsbnSemResultado")
    void deveBuscarPorIsbnSemResultado() {
        // WireMock simula não encontrar resultado
        var resultado = livroService.buscarInformacoesExternas("0000000000000");

        assertFalse(resultado.encontrado());
        assertNull(resultado.mensagem());
    }

    @Test
    @DisplayName("deveRetornarMensagemQuandoGoogleBooksRetorna429")
    void deveRetornarMensagemQuandoGoogleBooksRetorna429() {
        // Simula limite de cota: WireMock retorna 429 para esse ISBN de teste
        var resultado = livroService.buscarInformacoesExternas("1111111111111");

        assertFalse(resultado.encontrado());
        assertNotNull(resultado.mensagem());
        assertTrue(resultado.mensagem().contains("Limite de consultas"));
    }

}

package com.example.biblioteca.service;

import com.example.biblioteca.AbstractIntegrationTest;
import com.example.biblioteca.dto.LivroRequestDTO;
import com.example.biblioteca.dto.LivroResponseDTO;
import com.example.biblioteca.model.Livro;
import com.example.biblioteca.repository.LivroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de Integração para LivroService (Caixa Branca + Caixa Preta)
 * - Sem uso de Mocks
 * - Utiliza Testcontainers para MongoDB real
 * - Valida persistência e lógica de negócios
 */
@DisplayName("LivroService - Testes de Integração")
class LivroServiceIntegrationIT extends AbstractIntegrationTest {

    @Autowired
    private LivroService livroService;

    @Autowired
    private LivroRepository livroRepository;

    private static final String EMAIL_TESTE = "usuario-teste@email.com";
    private static final String EMAIL_OUTRO_USUARIO = "outro-usuario@email.com";

    @BeforeEach
    void setUp() {
        livroRepository.deleteAll(); // Limpa o banco antes de cada teste
    }

    // ======== TESTES DE SUCESSO ========

    @Test
    @DisplayName("Deve salvar um novo livro com sucesso no MongoDB")
    void deveSalvarLivroComSucesso() {
        // Arrange
        LivroRequestDTO dto = new LivroRequestDTO(
                "Código Limpo",
                "Robert C. Martin",
                "9788576082675",
                "Tecnologia"
        );

        // Act
        LivroResponseDTO resultado = livroService.salvar(dto, EMAIL_TESTE);

        // Assert
        assertNotNull(resultado, "O resultado não deve ser nulo");
        assertNotNull(resultado.id(), "O ID do livro deve ser gerado");
        assertEquals("Código Limpo", resultado.titulo(), "O título deve ser igual ao enviado");
        assertEquals("Robert C. Martin", resultado.autor(), "O autor deve ser igual ao enviado");
        assertEquals("9788576082675", resultado.isbn(), "O ISBN deve ser igual ao enviado");
        assertEquals("Tecnologia", resultado.genero(), "O gênero deve ser igual ao enviado");

        // Validação adicional: verificar se foi persistido no banco
        Optional<Livro> livroSalvo = livroRepository.findById(resultado.id());
        assertTrue(livroSalvo.isPresent(), "O livro deve estar persistido no banco");
        assertEquals(EMAIL_TESTE, livroSalvo.get().getUsuarioId(), "O ID do usuário deve ser o email");
    }

    @Test
    @DisplayName("Deve listar livros do usuário que os cadastrou")
    void deveListarLivrosPorUsuario() {
        // Arrange: Cria 3 livros para usuário 1 e 2 para usuário 2
        LivroRequestDTO livro1 = new LivroRequestDTO("Livro 1", "Autor A", "isbn1", "Gênero1");
        LivroRequestDTO livro2 = new LivroRequestDTO("Livro 2", "Autor B", "isbn2", "Gênero2");
        LivroRequestDTO livro3 = new LivroRequestDTO("Livro 3", "Autor C", "isbn3", "Gênero3");
        LivroRequestDTO livro4 = new LivroRequestDTO("Livro 4", "Autor D", "isbn4", "Gênero4");

        livroService.salvar(livro1, EMAIL_TESTE);
        livroService.salvar(livro2, EMAIL_TESTE);
        livroService.salvar(livro3, EMAIL_TESTE);
        livroService.salvar(livro4, EMAIL_OUTRO_USUARIO);

        // Act
        List<LivroResponseDTO> livrosUsuario1 = livroService.listarPorUsuario(EMAIL_TESTE);
        List<LivroResponseDTO> livrosUsuario2 = livroService.listarPorUsuario(EMAIL_OUTRO_USUARIO);

        // Assert
        assertEquals(3, livrosUsuario1.size(), "Usuário 1 deve ter 3 livros");
        assertEquals(1, livrosUsuario2.size(), "Usuário 2 deve ter 1 livro");
        assertTrue(livrosUsuario1.stream().anyMatch(l -> l.titulo().equals("Livro 1")), "Livro 1 deve estar na lista");
        assertTrue(livrosUsuario2.stream().anyMatch(l -> l.titulo().equals("Livro 4")), "Livro 4 deve estar na lista");
    }

    @Test
    @DisplayName("Deve listar todos os livros cadastrados no sistema")
    void deveListarTodosOsLivros() {
        // Arrange
        LivroRequestDTO livro1 = new LivroRequestDTO("Livro A", "Autor A", "isbn-a", "GêneroA");
        LivroRequestDTO livro2 = new LivroRequestDTO("Livro B", "Autor B", "isbn-b", "GêneroB");

        livroService.salvar(livro1, EMAIL_TESTE);
        livroService.salvar(livro2, EMAIL_OUTRO_USUARIO);

        // Act
        List<LivroResponseDTO> todos = livroService.listarTodos();

        // Assert
        assertEquals(2, todos.size(), "Deve ter 2 livros no total");
        assertTrue(todos.stream().anyMatch(l -> l.titulo().equals("Livro A")), "Livro A deve estar na lista");
        assertTrue(todos.stream().anyMatch(l -> l.titulo().equals("Livro B")), "Livro B deve estar na lista");
    }

    // ======== TESTES PARAMETRIZADOS (MÚLTIPLOS CENÁRIOS) ========

    @ParameterizedTest
    @DisplayName("Deve salvar livros com diferentes gêneros")
    @ValueSource(strings = {"Ficção Científica", "Romance", "Tecnologia", "História", "Educação"})
    void deveSalvarLivrosComDiferentesGeneros(String genero) {
        // Arrange
        LivroRequestDTO dto = new LivroRequestDTO(
                "Livro Test - " + genero,
                "Autor Test",
                "isbn-" + genero,
                genero
        );

        // Act
        LivroResponseDTO resultado = livroService.salvar(dto, EMAIL_TESTE);

        // Assert
        assertEquals(genero, resultado.genero(), "O gênero deve ser: " + genero);
        assertTrue(livroRepository.findByUsuarioId(EMAIL_TESTE).stream()
                        .anyMatch(l -> l.getGenero().equals(genero)),
                "Livro com gênero " + genero + " deve estar no banco");
    }

    @ParameterizedTest
    @DisplayName("Deve salvar livros de diferentes autores")
    @ValueSource(strings = {"Robert C. Martin", "Kent Beck", "George R.R. Martin", "J.K. Rowling", "Isaac Asimov"})
    void deveSalvarLivrosDeAutoresDiferentes(String autor) {
        // Arrange
        LivroRequestDTO dto = new LivroRequestDTO(
                "Livro de " + autor,
                autor,
                "isbn-" + autor,
                "Gênero"
        );

        // Act
        LivroResponseDTO resultado = livroService.salvar(dto, EMAIL_TESTE);

        // Assert
        assertEquals(autor, resultado.autor(), "O autor deve ser: " + autor);
    }

    // ======== TESTES DE BUSCA E VALIDAÇÃO ========

    @Test
    @DisplayName("Deve buscar informações externas de um livro pelo ISBN")
    void deveBuscarInformacoesExternasDoLivro() {
        // Esse teste valida que o método não lança exceção
        // O WireMock está configurado para responder com dados mock
        assertDoesNotThrow(
                () -> livroService.buscarInformacoesExternas("9788576082675"),
                "Não deve lançar exceção ao buscar dados externos"
        );
    }

    @Test
    @DisplayName("Deve retornar lista vazia ao listar livros de usuário que não tem nenhum")
    void deveRetornarListaVaziaParaUsuarioSemLivros() {
        // Arrange
        String usuarioSemLivros = "usuario-sem-livros@email.com";

        // Act
        List<LivroResponseDTO> resultado = livroService.listarPorUsuario(usuarioSemLivros);

        // Assert
        assertTrue(resultado.isEmpty(), "A lista deve estar vazia");
        assertEquals(0, resultado.size(), "O tamanho deve ser 0");
    }

    // ======== TESTES DE ISOLAMENTO POR USUÁRIO ========

    @Test
    @DisplayName("Usuários não devem ver livros uns dos outros")
    void usuariosNaoDevemVerLivrosUnsDosOutros() {
        // Arrange
        LivroRequestDTO livroUser1 = new LivroRequestDTO("Livro do User 1", "Autor", "isbn1", "Gênero");
        LivroRequestDTO livroUser2 = new LivroRequestDTO("Livro do User 2", "Autor", "isbn2", "Gênero");

        String user1 = "user1@email.com";
        String user2 = "user2@email.com";

        livroService.salvar(livroUser1, user1);
        livroService.salvar(livroUser2, user2);

        // Act
        List<LivroResponseDTO> livrosUser1 = livroService.listarPorUsuario(user1);
        List<LivroResponseDTO> livrosUser2 = livroService.listarPorUsuario(user2);

        // Assert
        assertEquals(1, livrosUser1.size(), "User1 deve ter apenas 1 livro");
        assertEquals(1, livrosUser2.size(), "User2 deve ter apenas 1 livro");

        assertFalse(livrosUser1.stream().anyMatch(l -> l.titulo().contains("User 2")),
                "User1 não pode ver livros de User2");
        assertFalse(livrosUser2.stream().anyMatch(l -> l.titulo().contains("User 1")),
                "User2 não pode ver livros de User1");
    }

    // ======== TESTES DE PERSISTÊNCIA ========

    @Test
    @DisplayName("Deve persistir corretamente os dados no MongoDB")
    void devePersistirDadosNoMongoDB() {
        // Arrange
        LivroRequestDTO dto = new LivroRequestDTO(
                "Persistência Test",
                "Teste Autor",
                "isbn-persist-123",
                "Teste"
        );

        // Act
        LivroResponseDTO salvo = livroService.salvar(dto, EMAIL_TESTE);

        // Assert - Consulta direto no repositório para validar
        Livro livroNoBanco = livroRepository.findById(salvo.id()).orElse(null);
        assertNotNull(livroNoBanco, "Livro deve estar no banco");
        assertEquals("Persistência Test", livroNoBanco.getTitulo());
        assertEquals("Teste Autor", livroNoBanco.getAutor());
        assertEquals("isbn-persist-123", livroNoBanco.getIsbn());
        assertEquals("Teste", livroNoBanco.getGenero());
        assertEquals(EMAIL_TESTE, livroNoBanco.getUsuarioId());
    }

    @Test
    @DisplayName("Deve manter a integridade dos dados após múltiplas operações")
    void deveManteiIntegridadeDados() {
        // Arrange
        LivroRequestDTO[] livros = {
                new LivroRequestDTO("Livro 1", "Autor 1", "isbn1", "Gênero1"),
                new LivroRequestDTO("Livro 2", "Autor 2", "isbn2", "Gênero2"),
                new LivroRequestDTO("Livro 3", "Autor 3", "isbn3", "Gênero3")
        };

        // Act - Salva múltiplos livros
        for (LivroRequestDTO livro : livros) {
            livroService.salvar(livro, EMAIL_TESTE);
        }

        // Assert - Verifica se todos foram salvos corretamente
        List<LivroResponseDTO> resultados = livroService.listarPorUsuario(EMAIL_TESTE);
        assertEquals(3, resultados.size(), "Deve ter 3 livros");

        for (int i = 0; i < livros.length; i++) {
            assertEquals(livros[i].titulo(), resultados.get(i).titulo());
            assertEquals(livros[i].autor(), resultados.get(i).autor());
            assertEquals(livros[i].isbn(), resultados.get(i).isbn());
            assertEquals(livros[i].genero(), resultados.get(i).genero());
        }
    }
}

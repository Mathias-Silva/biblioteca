package com.example.biblioteca.service;

import static org.junit.jupiter.api.Assertions.*;

import com.example.biblioteca.AbstractIntegrationTest;
import com.example.biblioteca.dto.LivroRequestDTO;
import com.example.biblioteca.dto.LivroResponseDTO;
import com.example.biblioteca.model.Livro;
import com.example.biblioteca.repository.LivroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

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
        // Limpa o banco antes de cada teste (Testcontainers garante um MongoDB isolado)
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
        // Persistência real: salva no MongoDB dentro do container
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

        // Assert
        assertEquals(3, livrosUsuario1.size());
    }

}

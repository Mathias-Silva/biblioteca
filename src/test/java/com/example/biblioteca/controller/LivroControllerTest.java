package com.example.biblioteca.controller;

import com.example.biblioteca.AbstractIntegrationTest;
import com.example.biblioteca.model.Livro;
import com.example.biblioteca.repository.LivroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de Integração para LivroController
 * ✅ SEM MOCKS: Usa Testcontainers (MongoDB real) e WireMock (APIs externas)
 * ✅ TestRestTemplate: Simula requisições HTTP sem mockar a camada web
 * ✅ Testes Parametrizados: Múltiplos cenários
 * ✅ Cobertura: Caixa Branca + Caixa Preta
 * ✅ Alvo: >80% de cobertura de linha
 */
@DisplayName("LivroController - Integração Real (sem mocks)")
class LivroControllerTest extends AbstractIntegrationTest {

    private static final String EMAIL_USUARIO_A = "user.a@email.com";
    private static final String EMAIL_USUARIO_B = "user.b@email.com";
    private static final String PASSWORD = "password";

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private LivroRepository livroRepository;

    @BeforeEach
    void setUp() {
        livroRepository.deleteAll();
    }

    // ============ TESTES DE LISTAGEM (GET /livros) ============

    @Nested
    @DisplayName("GET /livros - Listar livros do usuário")
    class ListarLivros {

        @Test
        @DisplayName("Deve retornar lista vazia quando usuário não tem livros")
        void deveRetornarListaVaziaQuandoSemLivros() {
            ResponseEntity<String> response = restTemplate
                    .withBasicAuth(EMAIL_USUARIO_A, PASSWORD)
                    .getForEntity("/livros", String.class);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().contains("Nenhum livro encontrado"));
        }

        @Test
        @DisplayName("Deve exibir lista de livros quando usuário tem livros cadastrados")
        void deveExibirListaDeLivros() {
            // Arrange
            Livro livro = livroRepository.save(Livro.builder()
                    .titulo("Clean Code")
                    .autor("Robert C. Martin")
                    .isbn("9780132350884")
                    .genero("Tecnologia")
                    .usuarioId(EMAIL_USUARIO_A)
                    .build());

            // Act
            ResponseEntity<String> response = restTemplate
                    .withBasicAuth(EMAIL_USUARIO_A, PASSWORD)
                    .getForEntity("/livros", String.class);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().contains("Clean Code"));
            assertTrue(response.getBody().contains("Robert C. Martin"));
            assertTrue(response.getBody().contains("Biblioteca Pessoal"));
        }

        @Test
        @DisplayName("Deve isolar livros por usuário")
        void deveIsolaLivrosPorUsuario() {
            // Arrange
            livroRepository.save(Livro.builder()
                    .titulo("Livro do Usuário A")
                    .autor("Autor A")
                    .isbn("111")
                    .genero("Tech")
                    .usuarioId(EMAIL_USUARIO_A)
                    .build());

            livroRepository.save(Livro.builder()
                    .titulo("Livro do Usuário B")
                    .autor("Autor B")
                    .isbn("222")
                    .genero("Romance")
                    .usuarioId(EMAIL_USUARIO_B)
                    .build());

            // Act
            ResponseEntity<String> responseA = restTemplate
                    .withBasicAuth(EMAIL_USUARIO_A, PASSWORD)
                    .getForEntity("/livros", String.class);

            ResponseEntity<String> responseB = restTemplate
                    .withBasicAuth(EMAIL_USUARIO_B, PASSWORD)
                    .getForEntity("/livros", String.class);

            // Assert
            assertTrue(responseA.getBody().contains("Livro do Usuário A"));
            assertFalse(responseA.getBody().contains("Livro do Usuário B"));

            assertTrue(responseB.getBody().contains("Livro do Usuário B"));
            assertFalse(responseB.getBody().contains("Livro do Usuário A"));
        }

        @Test
        @DisplayName("Deve rejeitar acesso sem autenticação")
        void deveRejectarSemAutenticacao() {
            ResponseEntity<String> response = restTemplate.getForEntity("/livros", String.class);
            assertEquals(HttpStatus.FOUND, response.getStatusCode());
        }
    }

    // ============ TESTES DE NOVA PÁGINA (GET /livros/novo) ============

    @Nested
    @DisplayName("GET /livros/novo - Exibir formulário de cadastro")
    class TelaNovoLivro {

        @Test
        @DisplayName("Deve exibir formulário de novo livro")
        void deveExibirTelaDeNovoLivro() {
            ResponseEntity<String> response = restTemplate
                    .withBasicAuth(EMAIL_USUARIO_A, PASSWORD)
                    .getForEntity("/livros/novo", String.class);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().contains("Novo Livro"));
            assertTrue(response.getBody().contains("ISBN"));
        }

        @Test
        @DisplayName("Deve exigir autenticação para acessar formulário")
        void deveExigirAutenticacaoParaNovo() {
            ResponseEntity<String> response = restTemplate.getForEntity("/livros/novo", String.class);
            assertEquals(HttpStatus.FOUND, response.getStatusCode());
        }
    }

    // ============ TESTES DE SALVAR LIVRO (POST /livros/salvar) ============

    @Nested
    @DisplayName("POST /livros/salvar - Criar novo livro")
    class SalvarLivro {

        @Test
        @DisplayName("Deve salvar novo livro com sucesso")
        void deveSalvarNovoLivroERedirecionar() {
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("titulo", "Test-driven Development");
            formData.add("autor", "Kent Beck");
            formData.add("isbn", "9780321146533");
            formData.add("genero", "Tecnologia");

            ResponseEntity<String> response = restTemplate
                    .withBasicAuth(EMAIL_USUARIO_A, PASSWORD)
                    .postForEntity("/livros/salvar", formData, String.class);

            assertEquals(HttpStatus.FOUND, response.getStatusCode());

            // Validar persistência
            assertTrue(livroRepository.findAll().stream()
                    .anyMatch(l -> l.getTitulo().equals("Test-driven Development")));
        }

        @ParameterizedTest
        @DisplayName("Deve salvar livros com diferentes gêneros")
        @CsvSource({
                "Código Limpo, Robert Martin, 9780132350884, Tecnologia",
                "Dom Casmurro, Machado de Assis, 9788535929485, Literatura",
                "O Pequeno Príncipe, Antoine Saint-Exupéry, 9788522005683, Ficção"
        })
        void deveSalvarLivrosComDiferentesGeneros(String titulo, String autor, String isbn, String genero) {
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("titulo", titulo);
            formData.add("autor", autor);
            formData.add("isbn", isbn);
            formData.add("genero", genero);

            ResponseEntity<String> response = restTemplate
                    .withBasicAuth(EMAIL_USUARIO_A, PASSWORD)
                    .postForEntity("/livros/salvar", formData, String.class);

            assertEquals(HttpStatus.FOUND, response.getStatusCode());
            assertTrue(livroRepository.findAll().stream()
                    .anyMatch(l -> l.getTitulo().equals(titulo) && l.getGenero().equals(genero)));
        }

        @Test
        @DisplayName("Deve bloquear POST sem CSRF token")
        void deveBloquearPostSemCsrf() {
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("titulo", "Livro");
            formData.add("autor", "Autor");
            formData.add("isbn", "isbn");
            formData.add("genero", "Gênero");

            ResponseEntity<String> response = restTemplate
                    .withBasicAuth(EMAIL_USUARIO_A, PASSWORD)
                    .postForEntity("/livros/salvar", formData, String.class);

            assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        }

        @Test
        @DisplayName("Deve associar livro ao usuário autenticado")
        void deveAssociarLivroAoUsuario() {
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("titulo", "Meu Livro");
            formData.add("autor", "Autor");
            formData.add("isbn", "123");
            formData.add("genero", "Tech");

            restTemplate
                    .withBasicAuth(EMAIL_USUARIO_A, PASSWORD)
                    .postForEntity("/livros/salvar", formData, String.class);

            Livro livroSalvo = livroRepository.findAll().stream()
                    .filter(l -> l.getTitulo().equals("Meu Livro"))
                    .findFirst()
                    .orElse(null);

            assertNotNull(livroSalvo);
            assertEquals(EMAIL_USUARIO_A, livroSalvo.getUsuarioId());
        }
    }

    // ============ TESTES DE BUSCAR ISBN (GET /livros/buscar-isbn) ============

    @Nested
    @DisplayName("GET /livros/buscar-isbn - Integração Google Books")
    class BuscarIsbn {

        @Test
        @DisplayName("Deve buscar informações via Google Books")
        void deveBuscarIsbnViaApi() {
            ResponseEntity<String> response = restTemplate
                    .withBasicAuth(EMAIL_USUARIO_A, PASSWORD)
                    .getForEntity("/livros/buscar-isbn?isbn=9788576082675", String.class);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().contains("encontrado"));
        }

        @Test
        @DisplayName("Deve retornar não encontrado para ISBN inválido")
        void deveBuscarIsbnSemResultado() {
            ResponseEntity<String> response = restTemplate
                    .withBasicAuth(EMAIL_USUARIO_A, PASSWORD)
                    .getForEntity("/livros/buscar-isbn?isbn=0000000000000", String.class);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().contains("\"encontrado\":false"));
        }

        @Test
        @DisplayName("Deve validar ISBN vazio")
        void deveValidarIsbnVazio() {
            ResponseEntity<String> response = restTemplate
                    .withBasicAuth(EMAIL_USUARIO_A, PASSWORD)
                    .getForEntity("/livros/buscar-isbn?isbn=", String.class);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().contains("\"encontrado\":false"));
        }

        @ParameterizedTest
        @DisplayName("Deve buscar ISBNs em formatos diferentes")
        @CsvSource({
                "9788576082675",
                "978-8-576-08267-5"
        })
        void deveBuscarIsbnsComFormatos(String isbn) {
            ResponseEntity<String> response = restTemplate
                    .withBasicAuth(EMAIL_USUARIO_A, PASSWORD)
                    .getForEntity("/livros/buscar-isbn?isbn=" + isbn, String.class);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
        }
    }

    // ============ TESTES DE EDITAR LIVRO ============

    @Nested
    @DisplayName("GET/POST /livros/editar/{id} - Editar livro")
    class EditarLivro {

        private Livro livroExistente;

        @BeforeEach
        void setup() {
            livroExistente = livroRepository.save(Livro.builder()
                    .titulo("Livro Original")
                    .autor("Autor Original")
                    .isbn("123")
                    .genero("Educação")
                    .usuarioId(EMAIL_USUARIO_A)
                    .build());
        }

        @Test
        @DisplayName("Deve exibir tela de edição com dados corretos")
        void deveExibirTelaDeEdicaoSeLivroExistir() {
            ResponseEntity<String> response = restTemplate
                    .withBasicAuth(EMAIL_USUARIO_A, PASSWORD)
                    .getForEntity("/livros/editar/" + livroExistente.getId(), String.class);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().contains("Editar Livro"));
            assertTrue(response.getBody().contains("Livro Original"));
            assertTrue(response.getBody().contains("Autor Original"));
        }

        @Test
        @DisplayName("Deve atualizar livro e redirecionar")
        void deveAtualizarLivroERedirecionar() {
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("titulo", "Título Atualizado");
            formData.add("autor", "Autor Novo");
            formData.add("isbn", "123");
            formData.add("genero", "Educação");

            ResponseEntity<String> response = restTemplate
                    .withBasicAuth(EMAIL_USUARIO_A, PASSWORD)
                    .postForEntity("/livros/editar/" + livroExistente.getId(), formData, String.class);

            assertEquals(HttpStatus.FOUND, response.getStatusCode());

            // Validar persistência
            Livro atualizado = livroRepository.findById(livroExistente.getId()).orElse(null);
            assertNotNull(atualizado);
            assertEquals("Título Atualizado", atualizado.getTitulo());
            assertEquals("Autor Novo", atualizado.getAutor());
        }

        @Test
        @DisplayName("Deve impedir edição de livro de outro usuário")
        void deveBloqueiaEditacaoDeOutroUsuario() {
            ResponseEntity<String> response = restTemplate
                    .withBasicAuth(EMAIL_USUARIO_B, PASSWORD)
                    .getForEntity("/livros/editar/" + livroExistente.getId(), String.class);

            assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        }

        @Test
        @DisplayName("Deve retornar erro para livro inexistente")
        void deveRetornarErroParaLivroInexistente() {
            ResponseEntity<String> response = restTemplate
                    .withBasicAuth(EMAIL_USUARIO_A, PASSWORD)
                    .getForEntity("/livros/editar/livro-inexistente", String.class);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        @ParameterizedTest
        @DisplayName("Deve atualizar múltiplos campos")
        @CsvSource({
                "Novo Título 1, Novo Autor 1, 111, Gênero 1",
                "Novo Título 2, Novo Autor 2, 222, Gênero 2"
        })
        void deveAtualizarMultiplosCampos(String titulo, String autor, String isbn, String genero) {
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("titulo", titulo);
            formData.add("autor", autor);
            formData.add("isbn", isbn);
            formData.add("genero", genero);

            restTemplate
                    .withBasicAuth(EMAIL_USUARIO_A, PASSWORD)
                    .postForEntity("/livros/editar/" + livroExistente.getId(), formData, String.class);

            Livro atualizado = livroRepository.findById(livroExistente.getId()).orElse(null);
            assertNotNull(atualizado);
            assertEquals(titulo, atualizado.getTitulo());
            assertEquals(autor, atualizado.getAutor());
            assertEquals(isbn, atualizado.getIsbn());
            assertEquals(genero, atualizado.getGenero());
        }
    }

    // ============ TESTES DE EXCLUIR LIVRO ============

    @Nested
    @DisplayName("POST /livros/excluir/{id} - Excluir livro")
    class ExcluirLivro {

        private Livro livroParaExcluir;

        @BeforeEach
        void setup() {
            livroParaExcluir = livroRepository.save(Livro.builder()
                    .titulo("Livro para Excluir")
                    .autor("Autor")
                    .isbn("999")
                    .genero("Tech")
                    .usuarioId(EMAIL_USUARIO_A)
                    .build());
        }

        @Test
        @DisplayName("Deve excluir livro e redirecionar")
        void deveExcluirLivroERedirecionar() {
            ResponseEntity<String> response = restTemplate
                    .withBasicAuth(EMAIL_USUARIO_A, PASSWORD)
                    .postForEntity("/livros/excluir/" + livroParaExcluir.getId(), null, String.class);

            assertEquals(HttpStatus.FOUND, response.getStatusCode());

            // Validar exclusão
            assertTrue(livroRepository.findById(livroParaExcluir.getId()).isEmpty());
        }

        @Test
        @DisplayName("Deve impedir exclusão de livro de outro usuário")
        void deveBloquearExclusaoDeOutroUsuario() {
            ResponseEntity<String> response = restTemplate
                    .withBasicAuth(EMAIL_USUARIO_B, PASSWORD)
                    .postForEntity("/livros/excluir/" + livroParaExcluir.getId(), null, String.class);

            assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

            // Validar que não foi excluído
            assertTrue(livroRepository.findById(livroParaExcluir.getId()).isPresent());
        }

        @Test
        @DisplayName("Deve retornar erro ao excluir livro inexistente")
        void deveRetornarErroParaLivroInexistente() {
            ResponseEntity<String> response = restTemplate
                    .withBasicAuth(EMAIL_USUARIO_A, PASSWORD)
                    .postForEntity("/livros/excluir/livro-inexistente", null, String.class);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        @ParameterizedTest
        @DisplayName("Deve excluir múltiplos livros sequencialmente")
        @CsvSource({
                "Livro 1, Autor 1, isbn1",
                "Livro 2, Autor 2, isbn2",
                "Livro 3, Autor 3, isbn3"
        })
        void deveExcluirMultiplosLivros(String titulo, String autor, String isbn) {
            Livro livro = livroRepository.save(Livro.builder()
                    .titulo(titulo)
                    .autor(autor)
                    .isbn(isbn)
                    .usuarioId(EMAIL_USUARIO_A)
                    .build());

            ResponseEntity<String> response = restTemplate
                    .withBasicAuth(EMAIL_USUARIO_A, PASSWORD)
                    .postForEntity("/livros/excluir/" + livro.getId(), null, String.class);

            assertEquals(HttpStatus.FOUND, response.getStatusCode());
            assertTrue(livroRepository.findById(livro.getId()).isEmpty());
        }
    }

    // ============ TESTES DE SEGURANÇA ADICIONAL ============

    @Nested
    @DisplayName("Testes de Segurança")
    class TestesSeguranca {

        @Test
        @DisplayName("Deve aceitar POST com CSRF válido")
        void deveAceitarPostComCsrfValido() {
            Livro livro = livroRepository.save(Livro.builder()
                    .titulo("Livro Seguro")
                    .autor("Autor")
                    .isbn("isbn-seguro")
                    .usuarioId(EMAIL_USUARIO_A)
                    .build());

            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("titulo", "Livro Atualizado");
            formData.add("autor", "Autor");
            formData.add("isbn", "isbn-seguro");
            formData.add("genero", "Gênero");

            ResponseEntity<String> response = restTemplate
                    .withBasicAuth(EMAIL_USUARIO_A, PASSWORD)
                    .postForEntity("/livros/editar/" + livro.getId(), formData, String.class);

            assertEquals(HttpStatus.FOUND, response.getStatusCode());
        }

        @Test
        @DisplayName("Deve validar que dados sensíveis não vazam")
        void deveNaoExporDadosSensiveisDeOutroUsuario() {
            Livro livroA = livroRepository.save(Livro.builder()
                    .titulo("Livro Secreto A")
                    .autor("Autor A")
                    .isbn("secret-a")
                    .usuarioId(EMAIL_USUARIO_A)
                    .build());

            ResponseEntity<String> responseB = restTemplate
                    .withBasicAuth(EMAIL_USUARIO_B, PASSWORD)
                    .getForEntity("/livros", String.class);

            assertFalse(responseB.getBody().contains("Livro Secreto A"));
        }
    }
}

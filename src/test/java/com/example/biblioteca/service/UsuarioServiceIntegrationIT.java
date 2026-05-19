package com.example.biblioteca.service;

import com.example.biblioteca.AbstractIntegrationTest;
import com.example.biblioteca.exception.EmailJaCadastradoException;
import com.example.biblioteca.model.Usuario;
import com.example.biblioteca.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de Integração para UsuarioService
 * - Sem uso de Mocks
 * - Valida autenticação, cadastro e segurança de senhas
 */
@DisplayName("UsuarioService - Testes de Integração")
class UsuarioServiceIntegrationIT extends AbstractIntegrationTest {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @BeforeEach
    void setUp() {
        usuarioRepository.deleteAll();
    }

    // ======== TESTES DE CADASTRO ========

    @Test
    @DisplayName("Deve cadastrar um novo usuário com sucesso")
    void deveCadastrarUsuarioComSucesso() {
        // Arrange
        Usuario usuario = Usuario.builder()
                .nome("João Silva")
                .email("joao@email.com")
                .senha("SenhaForte123!")
                .build();

        // Act
        Usuario usuarioSalvo = usuarioService.cadastrar(usuario);

        // Assert
        assertNotNull(usuarioSalvo, "Usuário salvo não deve ser nulo");
        assertNotNull(usuarioSalvo.getId(), "ID do usuário deve ser gerado");
        assertEquals("João Silva", usuarioSalvo.getNome(), "Nome deve ser igual");
        assertEquals("joao@email.com", usuarioSalvo.getEmail(), "Email deve ser igual");

        // Validação adicional: verificar se foi persistido
        Optional<Usuario> usuarioNoBanco = usuarioRepository.findByEmail("joao@email.com");
        assertTrue(usuarioNoBanco.isPresent(), "Usuário deve estar no banco");
    }

    @Test
    @DisplayName("Deve rejeitar cadastro com e-mail duplicado")
    void deveRejeitarEmailDuplicado() {
        Usuario primeiro = Usuario.builder()
                .nome("Primeiro")
                .email("duplicado@email.com")
                .senha("Senha123!")
                .build();
        usuarioService.cadastrar(primeiro);

        Usuario segundo = Usuario.builder()
                .nome("Segundo")
                .email("duplicado@email.com")
                .senha("OutraSenha456!")
                .build();

        assertThrows(EmailJaCadastradoException.class, () -> usuarioService.cadastrar(segundo));
        assertEquals(1, usuarioRepository.count());
    }

    @Test
    @DisplayName("Deve tratar e-mail com maiúsculas como duplicata")
    void deveRejeitarEmailDuplicadoIgnorandoMaiusculas() {
        usuarioService.cadastrar(Usuario.builder()
                .nome("Um")
                .email("teste@email.com")
                .senha("Senha123!")
                .build());

        assertThrows(EmailJaCadastradoException.class, () -> usuarioService.cadastrar(Usuario.builder()
                .nome("Dois")
                .email("TESTE@email.com")
                .senha("Senha456!")
                .build()));
    }

    @Test
    @DisplayName("Deve criptografar a senha ao cadastrar")
    void deveCriptografarSenhaAoCadastrar() {
        // Arrange
        String senhaOriginal = "SenhaPlainText123!";
        Usuario usuario = Usuario.builder()
                .nome("Teste Criptografia")
                .email("cripto@email.com")
                .senha(senhaOriginal)
                .build();

        // Act
        Usuario usuarioSalvo = usuarioService.cadastrar(usuario);

        // Assert
        assertNotEquals(senhaOriginal, usuarioSalvo.getSenha(),
                "Senha não deve ser igual ao original (deve estar criptografada)");

        // Valida que a senha salva é diferente
        Optional<Usuario> usuarioNoBanco = usuarioRepository.findByEmail("cripto@email.com");
        assertTrue(usuarioNoBanco.isPresent());
        assertNotEquals(senhaOriginal, usuarioNoBanco.get().getSenha(),
                "Senha no banco deve estar criptografada");
    }

    @Test
    @DisplayName("Deve retornar o usuário salvo com ID gerado pelo banco")
    void deveRetornarUsuarioComIdGerado() {
        // Arrange
        Usuario usuario = Usuario.builder()
                .nome("Teste ID")
                .email("id-test@email.com")
                .senha("senha123")
                .build();

        // Act
        Usuario usuarioSalvo = usuarioService.cadastrar(usuario);

        // Assert
        assertNotNull(usuarioSalvo.getId(), "ID deve ser gerado pelo MongoDB");
        assertNotNull(usuarioSalvo.getId(), "ID não deve ser vazio");
    }

    // ======== TESTES DE AUTENTICAÇÃO ========

    @Test
    @DisplayName("Deve encontrar e autenticar um usuário registrado")
    void deveEncontrarUsuarioRegistrado() {
        // Arrange
        Usuario usuario = Usuario.builder()
                .nome("Usuário Auth")
                .email("auth@email.com")
                .senha("senha123")
                .build();
        usuarioService.cadastrar(usuario);

        // Act
        UserDetails userDetails = usuarioService.loadUserByUsername("auth@email.com");

        // Assert
        assertNotNull(userDetails, "UserDetails não deve ser nulo");
        assertEquals("auth@email.com", userDetails.getUsername(), "Email deve ser o username");
        assertNotNull(userDetails.getPassword(), "Senha não deve ser nula");
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar autenticar usuário inexistente")
    void deveLancarExcecaoParaUsuarioInexistente() {
        // Arrange
        String emailInexistente = "inexistente@email.com";

        // Act & Assert
        assertThrows(UsernameNotFoundException.class,
                () -> usuarioService.loadUserByUsername(emailInexistente),
                "Deve lançar UsernameNotFoundException para usuário inexistente");
    }

    // ======== TESTES PARAMETRIZADOS ========

    @ParameterizedTest
    @DisplayName("Deve cadastrar usuários com diferentes emails válidos")
    @ValueSource(strings = {
            "usuario1@email.com",
            "usuario2@gmail.com",
            "usuario3@hotmail.com",
            "usuario.nome@empresa.com.br",
            "usuario+tag@email.com"
    })
    void deveCadastrarUsuariosComEmailsDiferentes(String email) {
        // Arrange
        Usuario usuario = Usuario.builder()
                .nome("Usuário Test")
                .email(email)
                .senha("SenhaTest123!")
                .build();

        // Act
        Usuario usuarioSalvo = usuarioService.cadastrar(usuario);

        // Assert
        assertEquals(email, usuarioSalvo.getEmail(), "Email deve ser: " + email);
        assertTrue(usuarioRepository.findByEmail(email).isPresent(),
                "Usuário com email " + email + " deve estar no banco");
    }

    @ParameterizedTest
    @DisplayName("Deve cadastrar usuários com diferentes nomes")
    @ValueSource(strings = {
            "João Silva",
            "Maria Santos",
            "Pedro Oliveira",
            "Ana Costa",
            "Carlos Ferreira"
    })
    void deveCadastrarUsuariosComNomesDiferentes(String nome) {
        // Arrange
        String email = nome.toLowerCase().replace(" ", ".") + "@email.com";
        Usuario usuario = Usuario.builder()
                .nome(nome)
                .email(email)
                .senha("SenhaTest123!")
                .build();

        // Act
        Usuario usuarioSalvo = usuarioService.cadastrar(usuario);

        // Assert
        assertEquals(nome, usuarioSalvo.getNome(), "Nome deve ser: " + nome);
    }

    // ======== TESTES DE ISOLAMENTO ========

    @Test
    @DisplayName("Cadastros de usuários diferentes não devem conflitar")
    void cadastrosDiferentesNaoDevemConflitar() {
        // Arrange
        Usuario usuario1 = Usuario.builder()
                .nome("Usuário Um")
                .email("usuario1@email.com")
                .senha("Senha123!")
                .build();

        Usuario usuario2 = Usuario.builder()
                .nome("Usuário Dois")
                .email("usuario2@email.com")
                .senha("Senha456!")
                .build();

        // Act
        usuarioService.cadastrar(usuario1);
        usuarioService.cadastrar(usuario2);

        // Assert
        assertEquals(2, usuarioRepository.count(), "Deve ter 2 usuários no banco");
        assertTrue(usuarioRepository.findByEmail("usuario1@email.com").isPresent());
        assertTrue(usuarioRepository.findByEmail("usuario2@email.com").isPresent());
    }

    @Test
    @DisplayName("Cada usuário deve ter sua própria senha criptografada")
    void cadaUsuarioDeveTerSuaSenhaCriptografada() {
        // Arrange
        Usuario usuario1 = Usuario.builder()
                .nome("User 1")
                .email("user1@email.com")
                .senha("SenhaUser1")
                .build();

        Usuario usuario2 = Usuario.builder()
                .nome("User 2")
                .email("user2@email.com")
                .senha("SenhaUser2")
                .build();

        // Act
        Usuario salvo1 = usuarioService.cadastrar(usuario1);
        Usuario salvo2 = usuarioService.cadastrar(usuario2);

        // Assert
        assertNotEquals(salvo1.getSenha(), salvo2.getSenha(),
                "Senhas criptografadas devem ser diferentes");
        assertNotEquals("SenhaUser1", salvo1.getSenha(), "Senha 1 não deve ser plaintext");
        assertNotEquals("SenhaUser2", salvo2.getSenha(), "Senha 2 não deve ser plaintext");
    }

    // ======== TESTES DE PERSISTÊNCIA ========

    @Test
    @DisplayName("Deve persistir corretamente os dados do usuário no MongoDB")
    void devePersistirDadosCorreatamente() {
        // Arrange
        Usuario usuario = Usuario.builder()
                .nome("Persistência Test")
                .email("persistencia@email.com")
                .senha("TestPassword123!")
                .build();

        // Act
        Usuario usuarioSalvo = usuarioService.cadastrar(usuario);

        // Assert - Busca direto no repositório
        Usuario usuarioNoBanco = usuarioRepository.findById(usuarioSalvo.getId()).orElse(null);
        assertNotNull(usuarioNoBanco, "Usuário deve estar no banco");
        assertEquals("Persistência Test", usuarioNoBanco.getNome());
        assertEquals("persistencia@email.com", usuarioNoBanco.getEmail());
    }

    @Test
    @DisplayName("Deve ser possível buscar usuário após cadastro")
    void deveSerPossivelBuscarUsuarioAposCadastro() {
        // Arrange
        Usuario usuario = Usuario.builder()
                .nome("Busca Test")
                .email("busca@email.com")
                .senha("senha123")
                .build();
        usuarioService.cadastrar(usuario);

        // Act
        Optional<Usuario> usuarioEncontrado = usuarioRepository.findByEmail("busca@email.com");

        // Assert
        assertTrue(usuarioEncontrado.isPresent(), "Usuário deve ser encontrado");
        assertEquals("Busca Test", usuarioEncontrado.get().getNome());
    }
}

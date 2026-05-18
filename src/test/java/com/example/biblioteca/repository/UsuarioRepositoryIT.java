package com.example.biblioteca.repository;

import com.example.biblioteca.AbstractIntegrationTest;
import com.example.biblioteca.model.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private UsuarioRepository usuarioRepository; // Repositório para acessar o MongoDB real

    @Test
    @DisplayName("Deve salvar um usuário e buscá-lo com sucesso pelo e-mail no MongoDB real")
    void devePersistirEBuscarUsuarioPorEmail() {
        // Cria um usuário de teste
        Usuario usuario = new Usuario();
        usuario.setNome("Mathias");
        usuario.setEmail("mathias.repo@email.com");
        usuario.setSenha("$2a$10$encodedPasswordHere"); // Senha já criptografada

        // Persiste o usuário no banco
        Usuario usuarioSalvo = usuarioRepository.save(usuario);
        assertNotNull(usuarioSalvo.getId()); // Valida que o ID foi gerado

        // Busca o usuário pelo e-mail
        Optional<Usuario> usuarioBuscado = usuarioRepository.findByEmail("mathias.repo@email.com");
        assertTrue(usuarioBuscado.isPresent()); // Deve encontrar o usuário
        assertEquals("Mathias", usuarioBuscado.get().getNome()); // Nome deve bater
    }
}

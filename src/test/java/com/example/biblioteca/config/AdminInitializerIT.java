package com.example.biblioteca.config;

import com.example.biblioteca.AbstractIntegrationTest;
import com.example.biblioteca.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("AdminInitializer - Testes de integração")
class AdminInitializerIT extends AbstractIntegrationTest {

    @Autowired
    private AdminInitializer adminInitializer;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @BeforeEach
    void setUp() {
        usuarioRepository.deleteAll();
    }

    @Test
    @DisplayName("Deve criar usuário admin quando não existe")
    void deveCriarAdminQuandoNaoExistir() {
        adminInitializer.run();

        var admin = usuarioRepository.findByEmail(AdminInitializer.ADMIN_EMAIL);
        assertTrue(admin.isPresent());
        assertEquals("Admin", admin.get().getNome());
        assertNotEquals("AdminSenha123", admin.get().getSenha());
    }

    @Test
    @DisplayName("Não deve duplicar admin se já existir")
    void naoDeveDuplicarAdminSeJaExistir() {
        adminInitializer.run();
        adminInitializer.run();

        assertEquals(1, usuarioRepository.count());
        assertTrue(usuarioRepository.existsByEmail(AdminInitializer.ADMIN_EMAIL));
    }
}

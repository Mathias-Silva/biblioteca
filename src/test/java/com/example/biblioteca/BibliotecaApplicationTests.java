package com.example.biblioteca;

import com.example.biblioteca.config.AdminInitializer;
import com.example.biblioteca.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BibliotecaApplicationTests extends AbstractIntegrationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    @DisplayName("Deve carregar o contexto Spring")
    void contextLoads() {
        assertNotNull(applicationContext, "O contexto Spring deve inicializar corretamente");
    }

    @Test
    @DisplayName("Deve criar admin na subida da aplicação")
    void deveCriarAdminNaSubidaDaAplicacao() {
        assertTrue(usuarioRepository.existsByEmail(AdminInitializer.ADMIN_EMAIL));
    }
}

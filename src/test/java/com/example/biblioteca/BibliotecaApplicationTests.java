package com.example.biblioteca;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class BibliotecaApplicationTests extends AbstractIntegrationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("Deve carregar o contexto Spring")
    void contextLoads() {
        // Teste simples de smoke: garante que o contexto Spring inicializa
        assertNotNull(applicationContext, "O contexto Spring deve inicializar corretamente");
    }
}

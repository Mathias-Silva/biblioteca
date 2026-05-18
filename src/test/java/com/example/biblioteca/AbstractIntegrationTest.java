package com.example.biblioteca;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;

// Classe base para todos os testes de integração
// - Sobe o contexto completo da aplicação
// - Configura o MockMvc para simular requisições HTTP
// - Usa o profile "test" para isolar ambiente de testes
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {
    // Não precisa ter código aqui, apenas centraliza as anotações
}

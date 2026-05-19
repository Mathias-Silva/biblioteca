package com.example.biblioteca;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Classe base para testes de integração sem mocks.
 * Sobe contexto Spring completo, MongoDB via Testcontainers e Google Books via WireMock.
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "app.admin.default-password=AdminSenha123",
                "api.google-books.url=http://localhost:${wiremock.server.port}/books/v1",
                "api.viacep.url=http://localhost:${wiremock.server.port}/ws"
        }
)
@Testcontainers
@Import(TestcontainersConfiguration.class)
@AutoConfigureWireMock(port = 0)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {
}

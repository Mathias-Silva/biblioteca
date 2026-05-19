package com.example.biblioteca.service;

import com.example.biblioteca.dto.LivroRequestDTO;
import com.example.biblioteca.dto.LivroResponseDTO;
import com.example.biblioteca.model.Livro;
import com.example.biblioteca.repository.LivroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Testcontainers
@SpringBootTest(properties = {
        "api.google-books.url=http://localhost:${wiremock.server.port}",
        "app.admin.default-password=AdminSenha123"
})
@AutoConfigureWireMock(port = 0)
class LivroServiceTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:latest");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private LivroService livroService;

    @MockBean
    private LivroRepository livroRepository;

    private Livro livro;

    @BeforeEach
    void setUp() {
        livro = Livro.builder()
                .id("1")
                .titulo("Código Limpo")
                .autor("Robert C. Martin")
                .isbn("9788576082675")
                .genero("Computação")
                .usuarioId("user@email.com")
                .build();
    }

    @Test
    void deveSalvarLivroComSucesso() {
        LivroRequestDTO dto = new LivroRequestDTO("Código Limpo", "Robert C. Martin", "9788576082675", "Computação");
        when(livroRepository.save(any(Livro.class))).thenReturn(livro);

        LivroResponseDTO resultado = livroService.salvar(dto, "user@email.com");

        assertNotNull(resultado);
        assertEquals("Código Limpo", resultado.titulo());
        verify(livroRepository, times(1)).save(any(Livro.class));
    }

    @Test
    void deveListarPorUsuario() {
        when(livroRepository.findByUsuarioId("user@email.com")).thenReturn(List.of(livro));

        List<LivroResponseDTO> resultado = livroService.listarPorUsuario("user@email.com");

        assertFalse(resultado.isEmpty());
        assertEquals(1, resultado.size());
    }

    @Test
    void deveListarTodosOsLivros() {
        when(livroRepository.findAll()).thenReturn(List.of(livro));

        List<LivroResponseDTO> resultado = livroService.listarTodos();

        assertFalse(resultado.isEmpty());
        assertEquals(1, resultado.size());
    }

    @Test
    void deveBuscarInformacoesExternasComSucesso_UsandoWireMock() {
        assertDoesNotThrow(() -> livroService.buscarInformacoesExternas("9788576082675"));
    }
}
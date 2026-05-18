package com.example.biblioteca.service;

import com.example.biblioteca.AbstractIntegrationTest;
import com.example.biblioteca.dto.LivroRequestDTO;
import com.example.biblioteca.repository.LivroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

class LivroIntegrationIT extends AbstractIntegrationTest {

    @Autowired
    private LivroService livroService; // Serviço que contém a lógica de salvar livros

    @Autowired
    private LivroRepository livroRepository; // Repositório para validar persistência no MongoDB

    @BeforeEach
    void setUp() {
        livroRepository.deleteAll(); // Limpa o banco antes de cada teste
    }

    @Test
    @DisplayName("Deve buscar dados no VCR e persistir no MongoDB real")
    void deveExecutarFluxoCompletoSemMocks() {
        // 1. Cenário: ISBN do livro que já está gravado no JSON do VCR
        String isbn = "9788576082675";

        // 2. Ação: Cria DTO e chama o serviço que consulta o VCR (localhost:8081)
        LivroRequestDTO dto = new LivroRequestDTO("Código Limpo", "Robert C. Martin", isbn, "Tecnologia");
        var resposta = livroService.salvar(dto, "usuario-teste-123");

        // 3. Validação: O livro deve ter sido persistido no MongoDB do Testcontainers
        assertNotNull(resposta.id());
        assertTrue(livroRepository.findById(resposta.id()).isPresent(), "O livro deve estar no MongoDB");

        // 4. Validação extra: Os dados retornados devem bater com o 'cassete' do VCR
        assertEquals("Código Limpo", resposta.titulo());
        System.out.println("Sucesso! O VCR respondeu e o Testcontainers salvou o livro: " + resposta.id());
    }
}

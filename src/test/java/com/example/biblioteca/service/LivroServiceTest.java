package com.example.biblioteca.service;

import com.example.biblioteca.AbstractIntegrationTest;
import com.example.biblioteca.dto.LivroRequestDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class LivroServiceIT extends AbstractIntegrationTest {

    @Autowired
    private LivroService livroService; // Serviço que contém a lógica de salvar livros

    @Test
    void testeComBancoRealNoDocker() {
        // Cria um DTO representando os dados de um livro
        LivroRequestDTO dto = new LivroRequestDTO("O Senhor dos Anéis", "J.R.R. Tolkien", "123", "Fantasia");

        // Chama o serviço para salvar o livro no MongoDB do Testcontainers
        var resultado = livroService.salvar(dto, "admin@email.com");

        // Valida se o livro foi realmente persistido (id não pode ser nulo)
        assertNotNull(resultado.id());
        System.out.println("Livro salvo no banco temporário com ID: " + resultado.id());
    }
}

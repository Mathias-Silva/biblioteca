package com.example.biblioteca.repository;

import com.example.biblioteca.AbstractIntegrationTest;
import com.example.biblioteca.dto.LivroRequestDTO;
import com.example.biblioteca.service.LivroService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class LivroRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private LivroService service; // Serviço que contém a lógica de salvar livros

    @Test
    void testeRealComBancoNoContainer() {
        // Cria um DTO representando os dados de um livro
        LivroRequestDTO dto = new LivroRequestDTO("Real", "Autor", "999", "Tech");

        // Chama o serviço para salvar o livro no MongoDB do Testcontainers
        var resp = service.salvar(dto, "user_real");

        // Valida se o livro foi realmente persistido (id não pode ser nulo)
        assertNotNull(resp.id());
    }
}

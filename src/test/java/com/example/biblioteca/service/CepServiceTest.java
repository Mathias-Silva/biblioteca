package com.example.biblioteca.service;

import com.example.biblioteca.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CepService - Testes de integração sem mocks")
class CepServiceTest extends AbstractIntegrationTest {

    @Autowired
    private CepService cepService;

    @Test
    @DisplayName("deveBuscarCepComSucesso_UsandoWireMock")
    void deveBuscarCepComSucesso_UsandoWireMock() {
        var resultado = cepService.buscar("01001-000");

        assertTrue(resultado.isPresent());
        assertEquals("Praça da Sé", resultado.get().logradouro());
        assertEquals("São Paulo", resultado.get().cidade());
        assertEquals("SP", resultado.get().estado());
    }

    @Test
    @DisplayName("deveRetornarVazioParaCepInvalido")
    void deveRetornarVazioParaCepInvalido() {
        assertTrue(cepService.buscar("123").isEmpty());
    }
}

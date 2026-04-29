package com.example.biblioteca.service;

import com.example.biblioteca.TestcontainersConfiguration;
import com.example.biblioteca.dto.LivroRequestDTO;
import com.example.biblioteca.service.LivroService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static com.mongodb.assertions.Assertions.assertNotNull;

@SpringBootTest
@Import(TestcontainersConfiguration.class) // Importa a configuração que você criou
class LivroServiceIT {

    @Autowired
    private LivroService livroService;

    @Test
    void testeComBancoRealNoDocker() {
        // Agora o livroService está usando o MongoDB do Testcontainers!
        // Criar um DTO de teste
        LivroRequestDTO dto = new LivroRequestDTO("O Senhor dos Anéis", "J.R.R. Tolkien", "123", "Fantasia");

        // Tentar salvar usando o serviço
        var resultado = livroService.salvar(dto, "admin@email.com");

        // Verificar se funcionou
        assertNotNull(resultado.id());
        System.out.println("Livro salvo no banco temporário com ID: " + resultado.id());

    }
}
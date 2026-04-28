package com.example.biblioteca.service;

import com.example.biblioteca.TestcontainersConfiguration;
import com.example.biblioteca.service.LivroService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestcontainersConfiguration.class) // Importa a configuração que você criou
class LivroServiceIT {

    @Autowired
    private LivroService livroService;

    @Test
    void testeComBancoRealNoDocker() {
        // Agora o livroService está usando o MongoDB do Testcontainers!
    }
}
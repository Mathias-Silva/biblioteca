package com.example.biblioteca.repository;

import com.example.biblioteca.dto.LivroRequestDTO;
import com.example.biblioteca.service.LivroService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Testcontainers
class LivroRepositoryIT {

    @Container
    static MongoDBContainer mongo = new MongoDBContainer("mongo:6.0");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
    }

    @Autowired
    private LivroService service;

    @Test
    void testeRealComBancoNoContainer() {
        LivroRequestDTO dto = new LivroRequestDTO("Real", "Autor", "999", "Tech");
        var resp = service.salvar(dto, "user_real");
        assertNotNull(resp.id());
    }
}
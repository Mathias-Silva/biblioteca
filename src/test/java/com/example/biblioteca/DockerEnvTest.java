package com.example.biblioteca;

import org.junit.jupiter.api.Test;

class DockerEnvTest {

    @Test
    void printDockerHostEnv() {
        // Lê a variável de ambiente DOCKER_HOST do sistema
        String dockerHost = System.getenv("DOCKER_HOST");

        // Imprime o valor no console (útil para debug)
        System.out.println("DOCKER_HOST = " + dockerHost);
    }
}

package com.example.biblioteca;

import org.junit.jupiter.api.Test;

public class DockerEnvTest {

    @Test
    void printDockerHostEnv() {
        String dockerHost = System.getenv("DOCKER_HOST");
        System.out.println("DOCKER_HOST = " + dockerHost);
    }
}

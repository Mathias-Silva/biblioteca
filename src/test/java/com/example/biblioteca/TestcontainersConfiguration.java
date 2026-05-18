package com.example.biblioteca;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

	@Bean
	@ServiceConnection // Conecta automaticamente o MongoDB do Testcontainers ao Spring
	MongoDBContainer mongoDbContainer() {
		// Sobe um container MongoDB versão 6.0 para os testes
		return new MongoDBContainer(DockerImageName.parse("mongo:6.0"));
	}

	@Bean(initMethod = "start", destroyMethod = "stop")
	WireMockServer wireMockServer() {
		// Configura o WireMock (VCR) na porta 8081
		// Usado para simular chamadas externas e gravar/reproduzir respostas
		return new WireMockServer(WireMockConfiguration.wireMockConfig()
				.port(8081)
				.usingFilesUnderClasspath("vcr")); // Pasta src/test/resources/vcr
	}
}

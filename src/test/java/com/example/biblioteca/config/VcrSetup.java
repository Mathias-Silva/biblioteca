package com.example.biblioteca.config;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.SingleRootFileSource;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

public class VcrSetup {
    private static WireMockServer wireMockServer;

    public static void startVcr() {
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig()
                .port(8081) // Define a porta onde o servidor WireMock vai rodar
                .usingFilesUnderClasspath("src/test/resources/vcr")); // Pasta onde ficam os "cassetes" (arquivos gravados)
        wireMockServer.start(); // Inicializa o servidor WireMock

        // Ativa a gravação automática das requisições/respostas em arquivos
        wireMockServer.enableRecordMappings(
                new SingleRootFileSource("src/test/resources/vcr/mappings"), // Onde salvar os mapeamentos das rotas
                new SingleRootFileSource("src/test/resources/vcr/__files")   // Onde salvar os arquivos de resposta
        );
    }

    public static void stopVcr() {
        wireMockServer.stop(); // Finaliza o servidor WireMock
    }
}

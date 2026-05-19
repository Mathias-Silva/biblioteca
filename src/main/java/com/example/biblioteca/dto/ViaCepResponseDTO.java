package com.example.biblioteca.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ViaCepResponseDTO(
        String cep,
        String logradouro,
        String complemento,
        String bairro,
        String localidade,
        String uf,
        @JsonProperty("erro") Boolean erro
) {}

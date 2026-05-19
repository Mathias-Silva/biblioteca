package com.example.biblioteca.dto;

public record CepLookupDTO(
        String cep,
        String logradouro,
        String complemento,
        String bairro,
        String cidade,
        String estado
) {}

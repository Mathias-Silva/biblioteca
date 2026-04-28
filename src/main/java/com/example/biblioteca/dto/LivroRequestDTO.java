package com.example.biblioteca.dto;

import jakarta.validation.constraints.NotBlank;

public record LivroRequestDTO(
        @NotBlank String titulo,
        @NotBlank String autor,
        String isbn,
        String genero
) {}
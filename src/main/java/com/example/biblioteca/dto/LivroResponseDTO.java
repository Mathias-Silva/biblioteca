package com.example.biblioteca.dto;

public record LivroResponseDTO(
        String id,
        String titulo,
        String autor,
        String isbn,
        String genero
) {}
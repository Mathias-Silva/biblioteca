package com.example.biblioteca.dto;

public record LivroIsbnLookupDTO(
        boolean encontrado,
        String titulo,
        String autor,
        String genero
) {
    public static LivroIsbnLookupDTO naoEncontrado() {
        return new LivroIsbnLookupDTO(false, null, null, null);
    }
}

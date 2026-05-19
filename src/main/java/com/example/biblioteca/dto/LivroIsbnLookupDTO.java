package com.example.biblioteca.dto;

public record LivroIsbnLookupDTO(
        boolean encontrado,
        String titulo,
        String autor,
        String genero,
        String mensagem
) {
    public static LivroIsbnLookupDTO naoEncontrado() {
        return new LivroIsbnLookupDTO(false, null, null, null, null);
    }

    public static LivroIsbnLookupDTO erroConsulta(String mensagem) {
        return new LivroIsbnLookupDTO(false, null, null, null, mensagem);
    }

    public static LivroIsbnLookupDTO encontrado(String titulo, String autor, String genero) {
        return new LivroIsbnLookupDTO(true, titulo, autor, genero, null);
    }
}

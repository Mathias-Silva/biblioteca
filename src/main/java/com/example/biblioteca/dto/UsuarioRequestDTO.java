
package com.example.biblioteca.dto;

// Altere os campos abaixo de acordo com o que o seu formulário de cadastro realmente envia
public record UsuarioRequestDTO(
        String nome,
        String email,
        String senha
) {}
package com.example.biblioteca.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "livros")
public class Livro {
    @Id
    private String id;

    @NotBlank(message = "O título não pode estar vazio")
    private String titulo;

    private String autor;
    private String isbn;
    private String genero;

    // ID do usuário que cadastrou o livro
    private String usuarioId;
}
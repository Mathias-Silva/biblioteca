package com.example.biblioteca.repository;

import com.example.biblioteca.model.Livro;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface LivroRepository extends MongoRepository<Livro, String> {
    // Busca personalizada: encontrar todos os livros de um usuário específico
    List<Livro> findByUsuarioId(String usuarioId);
}
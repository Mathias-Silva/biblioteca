package com.example.biblioteca.service;

import com.example.biblioteca.dto.LivroRequestDTO;
import com.example.biblioteca.dto.LivroResponseDTO;
import com.example.biblioteca.model.Livro;
import com.example.biblioteca.repository.LivroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LivroService {

    private final LivroRepository livroRepository;

    public LivroResponseDTO salvar(LivroRequestDTO dto, String usuarioId) {
        Livro livro = Livro.builder()
                .titulo(dto.titulo())
                .autor(dto.autor())
                .isbn(dto.isbn())
                .genero(dto.genero())
                .usuarioId(usuarioId)
                .build();

        Livro salvo = livroRepository.save(livro);
        return converterParaDTO(salvo);
    }


    public List<LivroResponseDTO> listarPorUsuario(String usuarioId) {
        return livroRepository.findByUsuarioId(usuarioId).stream()
                .map(this::converterParaDTO)
                .toList();
    }


    public List<LivroResponseDTO> listarTodos() {
        return livroRepository.findAll().stream()
                .map(this::converterParaDTO)
                .toList();
    }

    private LivroResponseDTO converterParaDTO(Livro livro) {
        return new LivroResponseDTO(
                livro.getId(),
                livro.getTitulo(),
                livro.getAutor(),
                livro.getIsbn(),
                livro.getGenero()
        );
    }
}
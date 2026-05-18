package com.example.biblioteca.service;

import com.example.biblioteca.dto.LivroRequestDTO;
import com.example.biblioteca.dto.LivroResponseDTO;
import com.example.biblioteca.model.Livro;
import com.example.biblioteca.repository.LivroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LivroService {

    private final LivroRepository livroRepository;
    private final RestTemplate restTemplate = new RestTemplate(); // Para chamadas HTTP

    @Value("${api.google-books.url}")
    private String googleBooksUrl;

    // Exemplo de como o VCR seria usado na prática
    public void buscarInformacoesExternas(String isbn) {
        // Quando o teste rodar, googleBooksUrl será http://localhost:8081
        // O WireMock (VCR) vai interceptar essa chamada
        String url = googleBooksUrl + "/books/v1/volumes?q=isbn:" + isbn;
        Object resposta = restTemplate.getForObject(url, Object.class);
        // Lógica para processar o JSON do "cassete" que criamos no passo anterior
    }

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
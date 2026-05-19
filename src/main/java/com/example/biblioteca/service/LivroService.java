package com.example.biblioteca.service;

import com.example.biblioteca.dto.LivroRequestDTO;
import com.example.biblioteca.dto.LivroResponseDTO;
import com.example.biblioteca.dto.GoogleBooksResponseDTO;
import com.example.biblioteca.model.Livro;
import com.example.biblioteca.repository.LivroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.logging.Logger;

@Service // Define que esta classe é um serviço do Spring
@RequiredArgsConstructor // Cria construtor automático para atributos finais
public class LivroService {

    private final LivroRepository livroRepository;
    private final RestTemplate restTemplate = new RestTemplate(); // Cliente HTTP para chamadas externas
    private static final Logger logger = Logger.getLogger(LivroService.class.getName());

    @Value("${api.google-books.url}") // Injeta a URL configurada no application.properties
    private String googleBooksUrl;

    /**
     * Busca informações de um livro em uma API externa (Google Books) usando o ISBN.
     * Útil para enriquecer os dados do cadastro com informações automáticas.
     */
    public void buscarInformacoesExternas(String isbn) {
        String url = googleBooksUrl + "/books/v1/volumes?q=isbn:" + isbn;

        // Faz a requisição e mapeia a resposta para um DTO
        GoogleBooksResponseDTO resposta = restTemplate.getForObject(url, GoogleBooksResponseDTO.class);

        // Se encontrou dados, exibe no log
        if (resposta != null && resposta.items() != null && !resposta.items().isEmpty()) {
            GoogleBooksResponseDTO.VolumeInfo info = resposta.items().get(0).volumeInfo();

            logger.info(() -> String.format(
                    "Livro encontrado! Título: %s | Autor(es): %s | Editora: %s",
                    info.title(),
                    String.join(", ", info.authors()),
                    info.publisher()
            ));
        } else {
            logger.warning(() -> "Nenhum livro encontrado para o ISBN informado.");
        }
    }

    // Salva um novo livro associado ao usuário
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

    // Lista livros de um usuário específico
    public List<LivroResponseDTO> listarPorUsuario(String usuarioId) {
        return livroRepository.findByUsuarioId(usuarioId).stream()
                .map(this::converterParaDTO)
                .toList();
    }

    // Lista todos os livros cadastrados
    public List<LivroResponseDTO> listarTodos() {
        return livroRepository.findAll().stream()
                .map(this::converterParaDTO)
                .toList();
    }

    // Converte entidade Livro para DTO de resposta
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

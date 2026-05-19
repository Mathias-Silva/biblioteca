package com.example.biblioteca.service;

import com.example.biblioteca.dto.GoogleBooksResponseDTO;
import com.example.biblioteca.dto.LivroIsbnLookupDTO;
import com.example.biblioteca.dto.LivroRequestDTO;
import com.example.biblioteca.dto.LivroResponseDTO;
import com.example.biblioteca.model.Livro;
import com.example.biblioteca.repository.LivroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.logging.Logger;

@Service // Define que esta classe é um serviço do Spring
@RequiredArgsConstructor // Cria construtor automático para atributos finais
public class LivroService {

    private final LivroRepository livroRepository;
    private final RestTemplate restTemplate = new RestTemplate(); // Cliente HTTP para chamadas externas
    private static final Logger logger = Logger.getLogger(LivroService.class.getName());

    @Value("${api.google-books.url}")
    private String googleBooksUrl;

    @Value("${api.google-books.api-key:}")
    private String googleBooksApiKey;

    /**
     * Busca informações de um livro na Google Books API usando o ISBN.
     */
    public LivroIsbnLookupDTO buscarInformacoesExternas(String isbn) {
        String isbnLimpo = isbn.replaceAll("[^0-9Xx]", "");
        if (isbnLimpo.isBlank()) {
            return LivroIsbnLookupDTO.naoEncontrado();
        }

        String url = montarUrlGoogleBooks(isbnLimpo);

        try {
            GoogleBooksResponseDTO resposta = restTemplate.getForObject(url, GoogleBooksResponseDTO.class);

            if (resposta == null || resposta.items() == null || resposta.items().isEmpty()) {
                logger.warning(() -> "Nenhum livro encontrado para o ISBN: " + isbnLimpo);
                return LivroIsbnLookupDTO.naoEncontrado();
            }

            GoogleBooksResponseDTO.VolumeInfo info = resposta.items().getFirst().volumeInfo();
            String autor = info.authors() != null && !info.authors().isEmpty()
                    ? String.join(", ", info.authors()) : "";
            String genero = info.categories() != null && !info.categories().isEmpty()
                    ? info.categories().getFirst() : "";

            logger.info(() -> String.format(
                    "Livro encontrado! Título: %s | Autor(es): %s",
                    info.title(), autor));

            return LivroIsbnLookupDTO.encontrado(info.title(), autor, genero);
        } catch (HttpStatusCodeException e) {
            if (e.getStatusCode().value() == 429) {
                logger.warning("Cota da Google Books excedida ao buscar ISBN: " + isbnLimpo);
                return LivroIsbnLookupDTO.erroConsulta(
                        "Limite de consultas à Google Books excedido. Tente mais tarde ou preencha manualmente.");
            }
            logger.warning(() -> "Google Books retornou HTTP " + e.getStatusCode().value() + " para ISBN: " + isbnLimpo);
            return LivroIsbnLookupDTO.erroConsulta(
                    "Não foi possível consultar a Google Books no momento. Tente novamente mais tarde.");
        } catch (RestClientException e) {
            logger.warning(() -> "Falha de rede ao consultar Google Books para ISBN " + isbnLimpo + ": " + e.getMessage());
            return LivroIsbnLookupDTO.erroConsulta(
                    "Não foi possível consultar a Google Books. Verifique sua conexão com a internet.");
        }
    }

    private String montarUrlGoogleBooks(String isbnLimpo) {
        String base = googleBooksUrl.endsWith("/")
                ? googleBooksUrl.substring(0, googleBooksUrl.length() - 1)
                : googleBooksUrl;
        StringBuilder url = new StringBuilder(base)
                .append("/volumes?q=isbn:")
                .append(isbnLimpo);
        if (StringUtils.hasText(googleBooksApiKey)) {
            url.append("&key=").append(googleBooksApiKey.trim());
        }
        return url.toString();
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

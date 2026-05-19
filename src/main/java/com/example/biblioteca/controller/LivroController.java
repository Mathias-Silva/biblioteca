package com.example.biblioteca.controller;

import com.example.biblioteca.dto.LivroRequestDTO;
import com.example.biblioteca.dto.LivroResponseDTO;
import com.example.biblioteca.model.Livro;
import com.example.biblioteca.repository.LivroRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.example.biblioteca.service.LivroService;

@Controller
@RequestMapping("/livros") // Define a rota base para todos os métodos deste controlador
@RequiredArgsConstructor // Gera automaticamente um construtor com os atributos finais
public class LivroController {

    // Constante para evitar duplicação de string em redirecionamentos
    private static final String REDIRECT_LIVROS = "redirect:/livros";

    private final LivroService livroService; // Serviço responsável pela lógica de negócios
    private final LivroRepository livroRepository; // Repositório para acesso ao banco de dados

    @GetMapping
    public String listar(Model model, @AuthenticationPrincipal UserDetails userDetails)  {
        // Obtém o e-mail do usuário logado
        String emailLogado = userDetails.getUsername();
        // Busca os livros cadastrados pelo usuário
        List<LivroResponseDTO> livros = livroService.listarPorUsuario(emailLogado);
        // Adiciona a lista de livros ao modelo para ser exibida na view
        model.addAttribute("livros", livros);
        return "livros"; // Retorna a página "livros.html"
    }

    @GetMapping("/novo")
    public String telaCadastro(Model model) {
        // Adiciona um objeto vazio para preencher o formulário de cadastro
        model.addAttribute("livro", new LivroRequestDTO("", "", "", ""));
        return "cadastro-livro"; // Retorna a página de cadastro
    }

    @PostMapping("/salvar")
    public String salvar(LivroRequestDTO dto, @AuthenticationPrincipal UserDetails userDetails) {
        // Salva o livro associado ao usuário logado
        livroService.salvar(dto, userDetails.getUsername());
        return REDIRECT_LIVROS; // Redireciona para a lista de livros
    }

    @PostMapping("/excluir/{id}")
    public String excluir(@PathVariable String id, @AuthenticationPrincipal UserDetails userDetails) {
        Livro livro = buscarLivroDoUsuario(id, userDetails.getUsername());
        livroRepository.deleteById(livro.getId());
        return REDIRECT_LIVROS;
    }

    @GetMapping("/editar/{id}")
    public String telaEditar(@PathVariable String id, Model model,
                             @AuthenticationPrincipal UserDetails userDetails) {
        Livro livro = buscarLivroDoUsuario(id, userDetails.getUsername());
        model.addAttribute("livro", livro);
        return "editar-livro";
    }

    @PostMapping("/editar/{id}")
    public String atualizar(@PathVariable String id, LivroRequestDTO dto,
                            @AuthenticationPrincipal UserDetails userDetails) {
        Livro existente = buscarLivroDoUsuario(id, userDetails.getUsername());
        Livro livro = Livro.builder()
                .id(id)
                .titulo(dto.titulo())
                .autor(dto.autor())
                .isbn(dto.isbn())
                .genero(dto.genero())
                .usuarioId(existente.getUsuarioId())
                .build();

        livroRepository.save(livro);
        return REDIRECT_LIVROS;
    }

    private Livro buscarLivroDoUsuario(String id, String emailUsuario) {
        Livro livro = livroRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Livro inválido: " + id));
        if (!livro.getUsuarioId().equals(emailUsuario)) {
            throw new AccessDeniedException("Acesso negado ao livro de outro usuário");
        }
        return livro;
    }
}

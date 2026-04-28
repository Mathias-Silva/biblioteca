package com.example.biblioteca.controller;
import com.example.biblioteca.dto.LivroRequestDTO;
import com.example.biblioteca.dto.LivroResponseDTO;
import com.example.biblioteca.model.Livro;
import com.example.biblioteca.repository.LivroRepository;
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

@RequestMapping("/livros")
@RequiredArgsConstructor
public class LivroController {

    private final LivroService livroService;
    private final LivroRepository livroRepository;

    @GetMapping
    public String listar(Model model, @AuthenticationPrincipal UserDetails userDetails)  {
        String emailLogado = userDetails.getUsername();
        List<LivroResponseDTO> livros = livroService.listarPorUsuario(emailLogado);
        model.addAttribute("livros", livros);
        return "livros";
    }
    @GetMapping("/novo") // Isso completa a URL /livros/novo
    public String telaCadastro(Model model) {
        // É importante passar um objeto vazio para o Thymeleaf não dar erro no formulário
        model.addAttribute("livro", new LivroRequestDTO("", "", "", ""));
        return "cadastro-livro"; // Deve ser o nome exato do arquivo em templates
    }
    @PostMapping("/salvar")
    public String salvar(LivroRequestDTO dto, @AuthenticationPrincipal UserDetails userDetails) {
        // Usamos o email do usuário logado como o "usuarioId"
        livroService.salvar(dto, userDetails.getUsername());
        return "redirect:/livros";
    }
    @PostMapping("/excluir/{id}")
    public String excluir(@PathVariable String id, @AuthenticationPrincipal UserDetails userDetails){
        livroRepository.deleteById(id);
        return "redirect:/livros";
    }
    @GetMapping("/editar/{id}")
    public String telaEditar(@PathVariable String id, Model model) {
        // Busca o livro no banco ou lança erro se não achar
        Livro livro = livroRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Livro inválido:" + id));

        model.addAttribute("livro", livro);
        return "editar-livro";
    }
    @PostMapping("/editar/{id}")
    public String atualizar(@PathVariable String id, LivroRequestDTO dto, @AuthenticationPrincipal UserDetails userDetails) {
        // No MongoDB, o save() funciona como "Update" se o ID já existir
        Livro livro = Livro.builder()
                .id(id) // IMPORTANTE: Manter o ID original para não criar um novo
                .titulo(dto.titulo())
                .autor(dto.autor())
                .isbn(dto.isbn())
                .genero(dto.genero())
                .usuarioId(userDetails.getUsername())
                .build();

        livroRepository.save(livro);
        return "redirect:/livros";
    }

}
package com.example.biblioteca.controller;

import com.example.biblioteca.dto.UsuarioRequestDTO;
import com.example.biblioteca.model.Usuario;
import com.example.biblioteca.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping("/login")
    public String login() {
        return "login"; // Retorna login.html
    }

    @GetMapping("/usuarios/cadastro")
    public String telaCadastro() {
        return "cadastro"; // Retorna cadastro.html
    }

    @PostMapping("/usuarios/cadastro")
    public String cadastrar(UsuarioRequestDTO dto) {
        // Converte o DTO seguro em uma Entidade antes de passar para o Service
        Usuario usuario = Usuario.builder()
                .nome(dto.nome())
                .email(dto.email())
                .senha(dto.senha())
                // Campos sensíveis como roles/permissões ficam protegidos aqui, sem exposição na Web
                .build();

        usuarioService.cadastrar(usuario);
        return "redirect:/login?sucesso"; // Redireciona para o login com aviso
    }
}
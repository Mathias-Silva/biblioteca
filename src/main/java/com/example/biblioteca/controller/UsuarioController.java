package com.example.biblioteca.controller;

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
    public String cadastrar(Usuario usuario) {
        usuarioService.cadastrar(usuario);
        return "redirect:/login?sucesso"; // Redireciona para o login com aviso
    }
}
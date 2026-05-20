package com.example.biblioteca.controller;

import com.example.biblioteca.dto.CepLookupDTO;
import com.example.biblioteca.dto.UsuarioRequestDTO;
import com.example.biblioteca.exception.EmailJaCadastradoException;
import com.example.biblioteca.model.Usuario;
import com.example.biblioteca.service.CepService;
import com.example.biblioteca.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final CepService cepService;

    @GetMapping("/login")
    public String login() {
        return "login"; // Retorna login.html
    }

    @GetMapping("/usuarios/cadastro")
    public String telaCadastro() {
        return "cadastro";
    }

    @GetMapping("/usuarios/buscar-cep")
    @ResponseBody
    public ResponseEntity<CepLookupDTO> buscarCep(@RequestParam String cep) {
        return cepService.buscar(cep)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/usuarios/cadastro")
    public String cadastrar(UsuarioRequestDTO dto) {
        Usuario usuario = Usuario.builder()
                .nome(dto.nome())
                .email(dto.email())
                .senha(dto.senha())
                .cep(dto.cep())
                .logradouro(dto.logradouro())
                .numero(dto.numero())
                .complemento(dto.complemento())
                .bairro(dto.bairro())
                .cidade(dto.cidade())
                .estado(dto.estado())
                .build();

        try {
            usuarioService.cadastrar(usuario);
        } catch (EmailJaCadastradoException e) {
            return "redirect:/usuarios/cadastro?erro=email";
        }
        return "redirect:/login?sucesso";
    }
}
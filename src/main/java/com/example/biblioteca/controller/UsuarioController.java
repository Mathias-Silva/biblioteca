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
        // Chama o serviço que faz a requisição ao ViaCEP (WireMock em testes fornece o stub)
        return cepService.buscar(cep)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/usuarios/cadastro")
    public String cadastrar(UsuarioRequestDTO dto) {
        // Constrói entidade Usuario a partir do DTO recebido do formulário
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
            // Tenta cadastrar; pode lançar EmailJaCadastradoException
            usuarioService.cadastrar(usuario);
        } catch (EmailJaCadastradoException e) {
            // Em caso de email duplicado redireciona com flag de erro
            return "redirect:/usuarios/cadastro?erro=email";
        }
        return "redirect:/login?sucesso";
    }
}

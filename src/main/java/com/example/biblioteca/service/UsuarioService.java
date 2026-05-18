package com.example.biblioteca.service;

import com.example.biblioteca.model.Usuario;
import com.example.biblioteca.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class UsuarioService implements UserDetailsService {

    private final UsuarioRepository repository;
    private final PasswordEncoder encoder;

    // AJUSTE: Mudou de void para Usuario para retornar o objeto com o ID do banco
    public Usuario cadastrar(Usuario usuario) {
        usuario.setSenha(encoder.encode(usuario.getSenha()));
        return repository.save(usuario); // Retorna o usuário salvo
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = repository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        return new User(usuario.getEmail(), usuario.getSenha(), new ArrayList<>());
    }
}
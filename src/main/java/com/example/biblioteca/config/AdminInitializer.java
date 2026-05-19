package com.example.biblioteca.config;

import com.example.biblioteca.model.Usuario;
import com.example.biblioteca.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    public static final String ADMIN_EMAIL = "admin@email.com";

    private static final Logger logger = Logger.getLogger(AdminInitializer.class.getName());

    private final UsuarioRepository repository;
    private final PasswordEncoder encoder;

    @Value("${app.admin.default-password:AdminSenha123}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        if (!repository.existsByEmail(ADMIN_EMAIL)) {
            Usuario admin = Usuario.builder()
                    .nome("Admin")
                    .email(ADMIN_EMAIL)
                    .senha(encoder.encode(adminPassword))
                    .build();
            repository.save(admin);
            logger.info("Usuário admin@email.com criado com sucesso!");
        }
    }
}

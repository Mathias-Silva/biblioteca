package com.example.biblioteca;

import com.example.biblioteca.model.Usuario;
import com.example.biblioteca.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.logging.Logger;

@SpringBootApplication
public class BibliotecaApplication {

	// Injeta a senha externa para evitar credenciais estáticas no código
	@Value("${app.admin.default-password}")
	private String adminPassword;

	public static void main(String[] args) {
		SpringApplication.run(BibliotecaApplication.class, args);
	}

	private static final Logger logger = Logger.getLogger(BibliotecaApplication.class.getName());

	@Bean
	@ConditionalOnBean(UsuarioRepository.class)
	CommandLineRunner init(UsuarioRepository repository, PasswordEncoder encoder) {
		return args -> {
			// Em vez de deletar tudo sem critério, verifica se o admin já existe
			if (!repository.existsByEmail("admin@email.com")) {
				Usuario user = new Usuario();
				user.setNome("Admin");
				user.setEmail("admin@email.com");
				user.setSenha(encoder.encode(adminPassword));
				repository.save(user);
				logger.info("Usuário admin@email.com criado com sucesso!");
			}
		};
	}
}
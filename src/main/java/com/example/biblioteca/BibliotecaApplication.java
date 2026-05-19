package com.example.biblioteca;

import com.example.biblioteca.model.Usuario;
import com.example.biblioteca.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
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
	CommandLineRunner init(UsuarioRepository repository, PasswordEncoder encoder) {
		return args -> {
			// 1. Limpa tudo para evitar o erro de duplicidade que está dando agora
			repository.deleteAll();

			// 2. Cria o usuário do zero, com a senha criptografada externa
			Usuario user = new Usuario();
			user.setNome("Admin");
			user.setEmail("admin@email.com");
			user.setSenha(encoder.encode(adminPassword));

			repository.save(user);
			logger.info("Banco resetado e usuário admin@email.com criado!");
		};
	}
}
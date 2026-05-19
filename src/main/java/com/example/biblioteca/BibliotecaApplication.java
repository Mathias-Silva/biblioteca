package com.example.biblioteca;

import com.example.biblioteca.model.Usuario;
import com.example.biblioteca.repository.UsuarioRepository;
import com.example.biblioteca.service.UsuarioService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.logging.Logger;

@SpringBootApplication
public class BibliotecaApplication {

	public static void main(String[] args) {
		SpringApplication.run(BibliotecaApplication.class, args);
	}
	Logger logger  = Logger.getLogger(getClass().getName());
	@Bean
	CommandLineRunner init(UsuarioRepository repository, PasswordEncoder encoder) {
		return args -> {
			// 1. Limpa tudo para evitar o erro de duplicidade que está dando agora
			repository.deleteAll();

			// 2. Cria o usuário do zero, com a senha criptografada
			Usuario user = new Usuario();
			user.setNome("Admin");
			user.setEmail("admin@email.com");
			user.setSenha(encoder.encode("AdminSenha123"));

			repository.save(user);
			logger.info("Banco resetado e usuário admin@email.com criado!");
		};
	}
}

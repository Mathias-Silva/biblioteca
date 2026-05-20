package com.example.biblioteca.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .httpBasic(AbstractHttpConfigurer::disable)

                // Configura autorizações: libera acesso a cadastro e recursos estáticos
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/usuarios/cadastro", "/usuarios/buscar-cep", "/css/**", "/js/**").permitAll()
                        .anyRequest().authenticated() // Tudo o resto exige login
                )
                .formLogin(form -> form
                        .loginPage("/login") // Nossa página de login customizada
                        .defaultSuccessUrl("/livros", true)
                        .permitAll()
                )
                .logout(LogoutConfigurer::permitAll);

        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        // Bean para criptografar senhas (BCrypt)
        return new BCryptPasswordEncoder();
    }
}

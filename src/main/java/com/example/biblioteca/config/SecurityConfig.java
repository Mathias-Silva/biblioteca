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
                // Desabilita explicitamente o CSRF usando a recomendação moderna do Spring Security
                .csrf(AbstractHttpConfigurer::disable)

                // CORREÇÃO SONARCLOUD: Desabilita explicitamente o HTTP Basic Authentication para sanar o Security Hotspot
                .httpBasic(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/usuarios/cadastro", "/css/**", "/js/**").permitAll() // Público
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
        return new BCryptPasswordEncoder(); // Para criptografar as senhas no banco
    }
}
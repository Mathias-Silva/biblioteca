package com.example.biblioteca.service;

import com.example.biblioteca.dto.CepLookupDTO;
import com.example.biblioteca.dto.ViaCepResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
public class CepService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${api.viacep.url}")
    private String viacepUrl;

    public Optional<CepLookupDTO> buscar(String cep) {
        // Remove tudo que não é dígito do CEP
        String apenasDigitos = cep.replaceAll("\\D", "");
        // Valida comprimento mínimo do CEP (8 dígitos no Brasil)
        if (apenasDigitos.length() != 8) {
            return Optional.empty();
        }

        // Normaliza URL base da API ViaCEP
        String base = viacepUrl.endsWith("/") ? viacepUrl.substring(0, viacepUrl.length() - 1) : viacepUrl;
        String url = base + "/" + apenasDigitos + "/json";

        // Chamada HTTP GET para ViaCEP (em testes, WireMock fornece o stub)
        ViaCepResponseDTO resposta = restTemplate.getForObject(url, ViaCepResponseDTO.class);
        if (resposta == null || Boolean.TRUE.equals(resposta.erro())) {
            // Retorna vazio quando ViaCEP não encontra o CEP ou retorna erro
            return Optional.empty();
        }

        // Constrói DTO de retorno com os campos de endereço
        return Optional.of(new CepLookupDTO(
                resposta.cep(),
                resposta.logradouro(),
                resposta.complemento(),
                resposta.bairro(),
                resposta.localidade(),
                resposta.uf()
        ));
    }
}

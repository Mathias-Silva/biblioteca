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
        String apenasDigitos = cep.replaceAll("\\D", "");
        if (apenasDigitos.length() != 8) {
            return Optional.empty();
        }

        String base = viacepUrl.endsWith("/") ? viacepUrl.substring(0, viacepUrl.length() - 1) : viacepUrl;
        String url = base + "/" + apenasDigitos + "/json";

        ViaCepResponseDTO resposta = restTemplate.getForObject(url, ViaCepResponseDTO.class);
        if (resposta == null || Boolean.TRUE.equals(resposta.erro())) {
            return Optional.empty();
        }

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

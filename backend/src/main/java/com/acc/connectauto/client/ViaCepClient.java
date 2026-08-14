package com.acc.connectauto.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.acc.connectauto.dto.ViaCepResponseDTO;

/**
 * Cliente HTTP para a API pública do ViaCEP (https://viacep.com.br/).
 */
@Component
public class ViaCepClient {

    private final RestClient restClient;

    // RestClient.Builder é autoconfigurado pelo Spring Boot (RestClientAutoConfiguration);
    // basta injetá-lo e fixar a base URL deste cliente específico.
    public ViaCepClient(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder
                .baseUrl("https://viacep.com.br/ws")
                .build();
    }

    public ViaCepResponseDTO buscarEnderecoPorCep(String cep) {
        return restClient.get()
                .uri("/{cep}/json/", cep)
                .retrieve()
                .body(ViaCepResponseDTO.class);
    }
}

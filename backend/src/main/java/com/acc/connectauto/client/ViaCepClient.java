package com.acc.connectauto.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.acc.connectauto.dto.ViaCepResponseDTO;

@Component
public class ViaCepClient {

    private final RestClient restClient;

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

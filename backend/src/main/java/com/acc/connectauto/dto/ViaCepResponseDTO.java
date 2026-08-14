package com.acc.connectauto.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ViaCepResponseDTO(
                String cep,
                String logradouro,
                String bairro,
                String localidade,
                String uf,
                Boolean erro) {
}

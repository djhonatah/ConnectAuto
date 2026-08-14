package com.acc.connectauto.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// ignoreUnknown = true: a resposta real do ViaCEP tem mais campos (complemento, ibge, ddd,
// siafi...) do que os usados aqui — sem essa anotação, o Jackson rejeitaria a resposta
// inteira por causa de propriedades "desconhecidas" para este DTO.
@JsonIgnoreProperties(ignoreUnknown = true)
public record ViaCepResponseDTO(
        String cep,
        String logradouro,
        String bairro,
        String localidade,
        String uf,
        // Presente e "true" somente quando o CEP não existe; ausente (null) em respostas
        // válidas. Ver issue #16, item "tratar CEP inválido/inexistente" (ainda não feito).
        Boolean erro
) {
}

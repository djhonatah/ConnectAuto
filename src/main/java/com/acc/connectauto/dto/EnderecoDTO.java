package com.acc.connectauto.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EnderecoDTO(

        @NotBlank(message = "logradouro é obrigatório")
        String logradouro,

        @NotBlank(message = "bairro é obrigatório")
        String bairro,

        @NotBlank(message = "cidade é obrigatória")
        String cidade,

        @NotBlank(message = "estado é obrigatório")
        @Size(min = 2, max = 2, message = "estado deve ter 2 caracteres (UF)")
        String estado,

        @NotBlank(message = "cep é obrigatório")
        @Size(min = 8, max = 8, message = "cep deve ter 8 dígitos")
        String cep
) {
}

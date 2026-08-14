package com.acc.connectauto.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// Usado tanto como request (DealerRequestDTO.endereco) quanto como response
// (DealerResponseDTO.endereco). Em POST/PUT /dealer, DealerService.resolverEnderecoOficial
// sobrescreve logradouro/bairro/cidade/estado com o retorno oficial do ViaCEP — só o cep
// digitado é preservado. Os valores enviados nesses 4 campos são validados (@NotBlank),
// mas não persistidos como enviados; o response reflete o endereço oficial resolvido.
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
        @Pattern(regexp = "\\d{8}", message = "cep deve ter 8 dígitos numéricos")
        String cep
) {
}

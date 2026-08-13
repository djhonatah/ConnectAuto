package com.acc.connectauto.dto.request;

import com.acc.connectauto.dto.EnderecoDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DealerRequestDTO(

                @NotBlank(message = "razaoSocial é obrigatória") String razaoSocial,

                @NotBlank(message = "cnpj é obrigatório") @Size(min = 14, max = 14, message = "cnpj deve ter 14 dígitos") String cnpj,

                @NotNull(message = "endereco é obrigatório")

                @Valid EnderecoDTO endereco) {
}

package com.acc.connectauto.dto.request;

import com.acc.connectauto.dto.EnderecoDTO;
import com.acc.connectauto.validation.CNPJ;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DealerRequestDTO(

                @NotBlank(message = "razaoSocial é obrigatória") String razaoSocial,

                @NotBlank(message = "cnpj é obrigatório") @CNPJ(message = "cnpj inválido (dígito verificador incorreto)") String cnpj,

                @NotNull(message = "endereco é obrigatório")

                @Valid EnderecoDTO endereco) {
}

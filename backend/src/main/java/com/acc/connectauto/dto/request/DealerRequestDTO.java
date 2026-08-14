package com.acc.connectauto.dto.request;

import org.hibernate.validator.constraints.br.CNPJ;

import com.acc.connectauto.dto.EnderecoDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record DealerRequestDTO(

                @NotBlank(message = "razaoSocial é obrigatória") String razaoSocial,

                @NotBlank(message = "cnpj é obrigatório")
                @Pattern(regexp = "\\d{14}", message = "cnpj deve ter 14 dígitos numéricos")
                @CNPJ(message = "cnpj inválido (dígito verificador incorreto)") String cnpj,

                @NotNull(message = "endereco é obrigatório")

                @Valid EnderecoDTO endereco) {
}

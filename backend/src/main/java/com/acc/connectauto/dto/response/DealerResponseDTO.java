package com.acc.connectauto.dto.response;

import com.acc.connectauto.dto.EnderecoDTO;

public record DealerResponseDTO(
        Long id,
        String razaoSocial,
        String cnpj,
        EnderecoDTO endereco
) {
}

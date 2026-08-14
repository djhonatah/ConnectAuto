package com.acc.connectauto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.acc.connectauto.dto.EnderecoDTO;
import com.acc.connectauto.dto.ViaCepResponseDTO;

@Mapper(componentModel = "spring")
public interface ViaCepMapper {

    @Mapping(target = "logradouro", source = "viaCepResponseDTO.logradouro")
    @Mapping(target = "bairro", source = "viaCepResponseDTO.bairro")
    @Mapping(target = "cidade", source = "viaCepResponseDTO.localidade")
    @Mapping(target = "estado", source = "viaCepResponseDTO.uf")
    @Mapping(target = "cep", source = "cep")
    EnderecoDTO toEnderecoDTO(ViaCepResponseDTO viaCepResponseDTO, String cep);
}

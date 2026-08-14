package com.acc.connectauto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.acc.connectauto.dto.request.DealerRequestDTO;
import com.acc.connectauto.dto.response.DealerResponseDTO;
import com.acc.connectauto.entity.Dealer;

@Mapper(componentModel = "spring")
public interface DealerMapper {

    @Mapping(target = "id", ignore = true)
    Dealer toEntity(DealerRequestDTO dealerRequestDTO);

    DealerResponseDTO toDTO(Dealer dealer);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromDto(DealerRequestDTO dealerRequestDTO, @MappingTarget Dealer dealer);
}

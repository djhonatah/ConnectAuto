package com.acc.connectauto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.acc.connectauto.dto.request.VehicleRequestDTO;
import com.acc.connectauto.dto.response.VehicleResponseDTO;
import com.acc.connectauto.entity.Vehicle;

@Mapper(componentModel = "spring")
public interface VehicleMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dealer", ignore = true)
    Vehicle toEntity(VehicleRequestDTO vehicleRequestDTO);

    @Mapping(source = "dealer.id", target = "dealerId")
    VehicleResponseDTO toDTO(Vehicle vehicle);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dealer", ignore = true)
    void updateEntityFromDto(VehicleRequestDTO vehicleRequestDTO, @MappingTarget Vehicle vehicle);
}

package com.acc.connectauto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.acc.connectauto.dto.request.VehicleRequestDTO;
import com.acc.connectauto.dto.response.VehicleResponseDTO;
import com.acc.connectauto.entity.Vehicle;

@Mapper(componentModel = "spring")
public interface VehicleMapper {
    @Mapping(target = "id", ignore = true)

    Vehicle toEntity(VehicleRequestDTO vehicleRequestDTO);

    VehicleResponseDTO toDTO(Vehicle vehicle);
}
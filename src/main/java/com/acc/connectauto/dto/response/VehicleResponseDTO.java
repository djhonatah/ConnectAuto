package com.acc.connectauto.dto.response;

import java.math.BigDecimal;

import com.acc.connectauto.entity.FuelType;

public record VehicleResponseDTO(
                Long id,
                String marca,
                String modelo,
                FuelType tipoCombustivel,
                String cor,
                Integer ano,
                String chassi,
                BigDecimal valor,
                String corInterna) {
}

package com.acc.connectauto.dto.request;

import java.math.BigDecimal;

import com.acc.connectauto.entity.FuelType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record VehicleRequestDTO(

                @NotBlank(message = "marca é obrigatória") String marca,

                @NotBlank(message = "modelo é obrigatório") String modelo,

                @NotNull(message = "tipoCombustivel é obrigatório") FuelType tipoCombustivel,

                @NotBlank(message = "cor é obrigatória") String cor,

                Integer ano,

                @Size(max = 17, message = "chassi deve ter no máximo 17 caracteres") String chassi,

                @PositiveOrZero(message = "valor não pode ser negativo") BigDecimal valor,

                String corInterna) {
}

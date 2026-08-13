package com.acc.connectauto.dto.request;

// Sem @NotNull em dealerId: enviar null é a forma de desassociar o veículo de qualquer
// concessionária (a associação é opcional, ver Vehicle.dealer).
public record VehicleDealerRequestDTO(Long dealerId) {
}

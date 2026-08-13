package com.acc.connectauto.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.acc.connectauto.entity.FuelType;
import com.acc.connectauto.entity.Vehicle;

@DataJpaTest
class VehicleRepositoryTest {

    @Autowired
    private VehicleRepository vehicleRepository;

    @Test
    void deveSalvarBuscarAtualizarEDeletarUmVehicle() {
        Vehicle vehicle = Vehicle.builder()
                .marca("Toyota")
                .modelo("Corolla")
                .tipoCombustivel(FuelType.FLEX)
                .cor("Prata")
                .ano(2024)
                .chassi("1HGCM82633A123456")
                .valor(new BigDecimal("120000.00"))
                .build();

        Vehicle salvo = vehicleRepository.save(vehicle);
        assertThat(salvo.getId()).isNotNull();

        Optional<Vehicle> encontrado = vehicleRepository.findById(salvo.getId());
        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getMarca()).isEqualTo("Toyota");

        salvo.setCor("Preto");
        vehicleRepository.save(salvo);
        assertThat(vehicleRepository.findById(salvo.getId()).get().getCor()).isEqualTo("Preto");

        vehicleRepository.deleteById(salvo.getId());
        assertThat(vehicleRepository.findById(salvo.getId())).isEmpty();
    }

    @Test
    void deveBuscarPorMarca() {
        vehicleRepository.save(Vehicle.builder()
                .marca("Honda").modelo("Civic").tipoCombustivel(FuelType.FLEX).cor("Branco")
                .build());
        vehicleRepository.save(Vehicle.builder()
                .marca("Honda").modelo("HR-V").tipoCombustivel(FuelType.HIBRIDO).cor("Preto")
                .build());
        vehicleRepository.save(Vehicle.builder()
                .marca("Toyota").modelo("Corolla").tipoCombustivel(FuelType.FLEX).cor("Prata")
                .build());

        List<Vehicle> hondas = vehicleRepository.findByMarca("Honda");

        assertThat(hondas).hasSize(2)
                .extracting(Vehicle::getModelo)
                .containsExactlyInAnyOrder("Civic", "HR-V");
    }

    @Test
    void deveBuscarPorTipoCombustivel() {
        vehicleRepository.save(Vehicle.builder()
                .marca("Renault").modelo("Kwid").tipoCombustivel(FuelType.ELETRICO).cor("Verde")
                .build());
        vehicleRepository.save(Vehicle.builder()
                .marca("Fiat").modelo("Mobi").tipoCombustivel(FuelType.FLEX).cor("Azul")
                .build());

        List<Vehicle> eletricos = vehicleRepository.findByTipoCombustivel(FuelType.ELETRICO);

        assertThat(eletricos).hasSize(1);
        assertThat(eletricos.get(0).getModelo()).isEqualTo("Kwid");
    }

    @Test
    void deveBuscarPorChassi() {
        vehicleRepository.save(Vehicle.builder()
                .marca("Jeep").modelo("Compass").tipoCombustivel(FuelType.DIESEL).cor("Cinza")
                .chassi("9BWZZZ377VT004251")
                .build());

        Optional<Vehicle> encontrado = vehicleRepository.findByChassi("9BWZZZ377VT004251");

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getModelo()).isEqualTo("Compass");
    }
}

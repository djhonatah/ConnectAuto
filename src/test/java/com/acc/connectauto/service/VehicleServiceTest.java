package com.acc.connectauto.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.acc.connectauto.dto.request.VehicleRequestDTO;
import com.acc.connectauto.dto.response.VehicleResponseDTO;
import com.acc.connectauto.entity.FuelType;
import com.acc.connectauto.exception.ResourceNotFoundException;

/**
 * Testes de integração de {@link VehicleService}, com contexto Spring e H2 reais.
 */
@SpringBootTest
@Transactional
class VehicleServiceTest {

    @Autowired
    private VehicleService vehicleService;

    private VehicleRequestDTO vehicleRequestDTOValido() {
        return new VehicleRequestDTO(
                "Toyota", "Corolla", FuelType.FLEX, "Prata",
                2024, "1HGCM82633A123456", new BigDecimal("120000.00"), null);
    }

    @Test
    void deveCriarVeiculo() {
        VehicleResponseDTO vehicleResponseDTO = vehicleService.criar(vehicleRequestDTOValido());

        assertThat(vehicleResponseDTO.id()).isNotNull();
        assertThat(vehicleResponseDTO.marca()).isEqualTo("Toyota");
        assertThat(vehicleResponseDTO.tipoCombustivel()).isEqualTo(FuelType.FLEX);
    }

    @Test
    void deveListarTodosOsVeiculos() {
        vehicleService.criar(vehicleRequestDTOValido());
        vehicleService.criar(vehicleRequestDTOValido());

        List<VehicleResponseDTO> vehicleResponseDTOs = vehicleService.listarTodos();

        assertThat(vehicleResponseDTOs).hasSize(2);
    }

    @Test
    void deveBuscarVeiculoPorId() {
        VehicleResponseDTO vehicleResponseDTO = vehicleService.criar(vehicleRequestDTOValido());

        VehicleResponseDTO foundVehicleResponseDTO = vehicleService.buscarPorId(vehicleResponseDTO.id());

        assertThat(foundVehicleResponseDTO.id()).isEqualTo(vehicleResponseDTO.id());
        assertThat(foundVehicleResponseDTO.modelo()).isEqualTo("Corolla");
    }

    @Test
    void deveLancarExcecaoAoBuscarIdInexistente() {
        assertThatThrownBy(() -> vehicleService.buscarPorId(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void deveAtualizarVeiculoExistente() {
        VehicleResponseDTO vehicleResponseDTO = vehicleService.criar(vehicleRequestDTOValido());
        VehicleRequestDTO vehicleAtualizacaoRequestDTO = new VehicleRequestDTO(
                "Toyota", "Corolla", FuelType.HIBRIDO, "Preto",
                2025, "1HGCM82633A123456", new BigDecimal("135000.00"), "Bege");

        VehicleResponseDTO updatedVehicleResponseDTO =
                vehicleService.atualizar(vehicleResponseDTO.id(), vehicleAtualizacaoRequestDTO);

        assertThat(updatedVehicleResponseDTO.id()).isEqualTo(vehicleResponseDTO.id());
        assertThat(updatedVehicleResponseDTO.cor()).isEqualTo("Preto");
        assertThat(updatedVehicleResponseDTO.tipoCombustivel()).isEqualTo(FuelType.HIBRIDO);
        assertThat(updatedVehicleResponseDTO.corInterna()).isEqualTo("Bege");
    }

    @Test
    void deveLancarExcecaoAoAtualizarIdInexistente() {
        assertThatThrownBy(() -> vehicleService.atualizar(999L, vehicleRequestDTOValido()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deveExcluirVeiculo() {
        VehicleResponseDTO vehicleResponseDTO = vehicleService.criar(vehicleRequestDTOValido());

        vehicleService.excluir(vehicleResponseDTO.id());

        assertThatThrownBy(() -> vehicleService.buscarPorId(vehicleResponseDTO.id()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deveLancarExcecaoAoExcluirIdInexistente() {
        assertThatThrownBy(() -> vehicleService.excluir(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}

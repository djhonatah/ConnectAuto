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

    private VehicleRequestDTO requestValido() {
        return new VehicleRequestDTO(
                "Toyota", "Corolla", FuelType.FLEX, "Prata",
                2024, "1HGCM82633A123456", new BigDecimal("120000.00"), null);
    }

    @Test
    void deveCriarVeiculo() {
        VehicleResponseDTO criado = vehicleService.criar(requestValido());

        assertThat(criado.id()).isNotNull();
        assertThat(criado.marca()).isEqualTo("Toyota");
        assertThat(criado.tipoCombustivel()).isEqualTo(FuelType.FLEX);
    }

    @Test
    void deveListarTodosOsVeiculos() {
        vehicleService.criar(requestValido());
        vehicleService.criar(requestValido());

        List<VehicleResponseDTO> todos = vehicleService.listarTodos();

        assertThat(todos).hasSize(2);
    }

    @Test
    void deveBuscarVeiculoPorId() {
        VehicleResponseDTO criado = vehicleService.criar(requestValido());

        VehicleResponseDTO encontrado = vehicleService.buscarPorId(criado.id());

        assertThat(encontrado.id()).isEqualTo(criado.id());
        assertThat(encontrado.modelo()).isEqualTo("Corolla");
    }

    @Test
    void deveLancarExcecaoAoBuscarIdInexistente() {
        assertThatThrownBy(() -> vehicleService.buscarPorId(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void deveAtualizarVeiculoExistente() {
        VehicleResponseDTO criado = vehicleService.criar(requestValido());
        VehicleRequestDTO atualizacao = new VehicleRequestDTO(
                "Toyota", "Corolla", FuelType.HIBRIDO, "Preto",
                2025, "1HGCM82633A123456", new BigDecimal("135000.00"), "Bege");

        VehicleResponseDTO atualizado = vehicleService.atualizar(criado.id(), atualizacao);

        assertThat(atualizado.id()).isEqualTo(criado.id());
        assertThat(atualizado.cor()).isEqualTo("Preto");
        assertThat(atualizado.tipoCombustivel()).isEqualTo(FuelType.HIBRIDO);
        assertThat(atualizado.corInterna()).isEqualTo("Bege");
    }

    @Test
    void deveLancarExcecaoAoAtualizarIdInexistente() {
        assertThatThrownBy(() -> vehicleService.atualizar(999L, requestValido()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deveExcluirVeiculo() {
        VehicleResponseDTO criado = vehicleService.criar(requestValido());

        vehicleService.excluir(criado.id());

        assertThatThrownBy(() -> vehicleService.buscarPorId(criado.id()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deveLancarExcecaoAoExcluirIdInexistente() {
        assertThatThrownBy(() -> vehicleService.excluir(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}

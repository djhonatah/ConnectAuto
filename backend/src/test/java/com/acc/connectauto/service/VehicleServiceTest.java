package com.acc.connectauto.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import com.acc.connectauto.client.ViaCepClient;
import com.acc.connectauto.dto.EnderecoDTO;
import com.acc.connectauto.dto.ViaCepResponseDTO;
import com.acc.connectauto.dto.request.DealerRequestDTO;
import com.acc.connectauto.dto.request.VehicleDealerRequestDTO;
import com.acc.connectauto.dto.request.VehicleRequestDTO;
import com.acc.connectauto.dto.response.DealerResponseDTO;
import com.acc.connectauto.dto.response.VehicleResponseDTO;
import com.acc.connectauto.entity.FuelType;
import com.acc.connectauto.exception.ResourceNotFoundException;

/**
 * Testes de integração de {@link VehicleService}, com contexto Spring e H2
 * reais.
 * {@link ViaCepClient} é substituído por um dublê (@MockitoBean) — os testes de
 * Vehicle
 * criam Dealers só como apoio, e não devem depender de uma chamada HTTP real ao
 * ViaCEP.
 */
@SpringBootTest
@Transactional
class VehicleServiceTest {

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private DealerService dealerService;

    @MockitoBean
    private ViaCepClient viaCepClient;

    @BeforeEach
    void configurarViaCepClient() {
        when(viaCepClient.buscarEnderecoPorCep(anyString())).thenAnswer(
                invocation -> new ViaCepResponseDTO("01310-100", "Av. Principal", "Centro", "São Paulo", "SP", null));
    }

    private VehicleRequestDTO vehicleRequestDTOValido() {
        return vehicleRequestDTOValido("1HGCM82633A123456");
    }

    private VehicleRequestDTO vehicleRequestDTOValido(String chassi) {
        return new VehicleRequestDTO(
                "Toyota", "Corolla", FuelType.FLEX, "Prata",
                2024, chassi, new BigDecimal("120000.00"), null, null);
    }

    private DealerResponseDTO criarDealer(String razaoSocial, String cnpj) {
        return dealerService.criar(new DealerRequestDTO(
                razaoSocial, cnpj,
                new EnderecoDTO("Av. Principal, 100", "Centro", "São Paulo", "SP", "01310100")));
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
        vehicleService.criar(vehicleRequestDTOValido("1HGCM82633A123456"));
        vehicleService.criar(vehicleRequestDTOValido("9BWZZZ377VT004251"));

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
                2025, "1HGCM82633A123456", new BigDecimal("135000.00"), "Bege", null);

        VehicleResponseDTO updatedVehicleResponseDTO = vehicleService.atualizar(vehicleResponseDTO.id(),
                vehicleAtualizacaoRequestDTO);

        assertThat(updatedVehicleResponseDTO.id()).isEqualTo(vehicleResponseDTO.id());
        assertThat(updatedVehicleResponseDTO.cor()).isEqualTo("Preto");
        assertThat(updatedVehicleResponseDTO.tipoCombustivel()).isEqualTo(FuelType.HIBRIDO);
        assertThat(updatedVehicleResponseDTO.corInterna()).isEqualTo("Bege");
    }

    @Test
    void deveRemoverAssociacaoComDealerAoAtualizarSemDealerId() {
        // PUT substitui o recurso inteiro: um veículo já associado a um dealer que recebe
        // um PUT sem dealerId perde a associação, de propósito (comportamento fixado por
        // este teste — para trocar só a associação sem esse efeito, use associarDealer).
        DealerResponseDTO dealerResponseDTO = criarDealer("Auto Center Toyota Ltda", "12345678000195");
        VehicleRequestDTO vehicleComDealerRequestDTO = new VehicleRequestDTO(
                "Toyota", "Corolla", FuelType.FLEX, "Prata",
                2024, "1HGCM82633A123456", new BigDecimal("120000.00"), null, dealerResponseDTO.id());
        VehicleResponseDTO vehicleResponseDTO = vehicleService.criar(vehicleComDealerRequestDTO);
        assertThat(vehicleResponseDTO.dealerId()).isEqualTo(dealerResponseDTO.id());

        VehicleRequestDTO vehicleAtualizacaoSemDealerRequestDTO = new VehicleRequestDTO(
                "Toyota", "Corolla", FuelType.FLEX, "Preto",
                2024, "1HGCM82633A123456", new BigDecimal("120000.00"), null, null);
        VehicleResponseDTO updatedVehicleResponseDTO =
                vehicleService.atualizar(vehicleResponseDTO.id(), vehicleAtualizacaoSemDealerRequestDTO);

        assertThat(updatedVehicleResponseDTO.dealerId()).isNull();
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

    @Test
    void deveCriarVeiculoComDealerAssociado() {
        DealerResponseDTO dealerResponseDTO = criarDealer("Auto Center Toyota Ltda", "12345678000195");

        VehicleRequestDTO vehicleComDealerRequestDTO = new VehicleRequestDTO(
                "Toyota", "Corolla", FuelType.FLEX, "Prata",
                2024, "1HGCM82633A123456", new BigDecimal("120000.00"), null, dealerResponseDTO.id());

        VehicleResponseDTO vehicleResponseDTO = vehicleService.criar(vehicleComDealerRequestDTO);

        assertThat(vehicleResponseDTO.dealerId()).isEqualTo(dealerResponseDTO.id());
    }

    @Test
    void deveCriarVeiculoSemDealerAssociado() {
        VehicleResponseDTO vehicleResponseDTO = vehicleService.criar(vehicleRequestDTOValido());

        assertThat(vehicleResponseDTO.dealerId()).isNull();
    }

    @Test
    void deveLancarExcecaoAoCriarVeiculoComDealerIdInexistente() {
        VehicleRequestDTO vehicleComDealerInexistenteRequestDTO = new VehicleRequestDTO(
                "Toyota", "Corolla", FuelType.FLEX, "Prata",
                2024, "1HGCM82633A123456", new BigDecimal("120000.00"), null, 999L);

        assertThatThrownBy(() -> vehicleService.criar(vehicleComDealerInexistenteRequestDTO))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deveAssociarDealerAUmVeiculoExistente() {
        VehicleResponseDTO vehicleResponseDTO = vehicleService.criar(vehicleRequestDTOValido());
        DealerResponseDTO dealerResponseDTO = criarDealer("Honda Sul Ltda", "11222333000181");

        VehicleResponseDTO updatedVehicleResponseDTO = vehicleService.associarDealer(
                vehicleResponseDTO.id(), new VehicleDealerRequestDTO(dealerResponseDTO.id()));

        assertThat(updatedVehicleResponseDTO.dealerId()).isEqualTo(dealerResponseDTO.id());
    }

    @Test
    void deveRemoverDealerDeUmVeiculoQuandoDealerIdForNull() {
        DealerResponseDTO dealerResponseDTO = criarDealer("Auto Center Toyota Ltda", "12345678000195");
        VehicleRequestDTO vehicleComDealerRequestDTO = new VehicleRequestDTO(
                "Toyota", "Corolla", FuelType.FLEX, "Prata",
                2024, "1HGCM82633A123456", new BigDecimal("120000.00"), null, dealerResponseDTO.id());
        VehicleResponseDTO vehicleResponseDTO = vehicleService.criar(vehicleComDealerRequestDTO);

        VehicleResponseDTO updatedVehicleResponseDTO = vehicleService.associarDealer(
                vehicleResponseDTO.id(), new VehicleDealerRequestDTO(null));

        assertThat(updatedVehicleResponseDTO.dealerId()).isNull();
    }

    @Test
    void deveLancarExcecaoAoAssociarDealerIdInexistente() {
        VehicleResponseDTO vehicleResponseDTO = vehicleService.criar(vehicleRequestDTOValido());
        VehicleDealerRequestDTO vehicleDealerRequestDTO = new VehicleDealerRequestDTO(999L);

        assertThatThrownBy(() -> vehicleService.associarDealer(vehicleResponseDTO.id(), vehicleDealerRequestDTO))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deveListarVeiculosDeUmaConcessionariaEspecifica() {
        DealerResponseDTO dealerResponseDTO = criarDealer("Auto Center Toyota Ltda", "12345678000195");
        DealerResponseDTO outroDealerResponseDTO = criarDealer("Honda Sul Ltda", "11222333000181");

        VehicleRequestDTO vehicleDoDealerRequestDTO = new VehicleRequestDTO(
                "Toyota", "Corolla", FuelType.FLEX, "Prata",
                2024, "1HGCM82633A123456", new BigDecimal("120000.00"), null, dealerResponseDTO.id());
        VehicleRequestDTO vehicleDeOutroDealerRequestDTO = new VehicleRequestDTO(
                "Honda", "Civic", FuelType.FLEX, "Branco",
                2024, "9BWZZZ377VT004251", new BigDecimal("130000.00"), null, outroDealerResponseDTO.id());

        vehicleService.criar(vehicleDoDealerRequestDTO);
        vehicleService.criar(vehicleDeOutroDealerRequestDTO);
        vehicleService.criar(vehicleRequestDTOValido("9BWZZZ377VT004999"));

        List<VehicleResponseDTO> vehiclesDoDealer = vehicleService.listarPorDealer(dealerResponseDTO.id());

        assertThat(vehiclesDoDealer).hasSize(1);
        assertThat(vehiclesDoDealer.get(0).modelo()).isEqualTo("Corolla");
    }

    @Test
    void deveLancarExcecaoAoListarVeiculosDeDealerIdInexistente() {
        assertThatThrownBy(() -> vehicleService.listarPorDealer(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}

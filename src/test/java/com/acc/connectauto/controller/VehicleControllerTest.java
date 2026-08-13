package com.acc.connectauto.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.acc.connectauto.dto.EnderecoDTO;
import com.acc.connectauto.dto.request.DealerRequestDTO;
import com.acc.connectauto.dto.request.VehicleDealerRequestDTO;
import com.acc.connectauto.dto.request.VehicleRequestDTO;
import com.acc.connectauto.dto.response.DealerResponseDTO;
import com.acc.connectauto.dto.response.VehicleResponseDTO;
import com.acc.connectauto.entity.FuelType;
import com.acc.connectauto.service.DealerService;
import com.acc.connectauto.service.VehicleService;

import tools.jackson.databind.ObjectMapper;

/**
 * Testes de integração dos endpoints HTTP de {@link VehicleController}, via MockMvc.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class VehicleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private DealerService dealerService;

    private VehicleRequestDTO vehicleRequestDTOValido() {
        return new VehicleRequestDTO(
                "Toyota", "Corolla", FuelType.FLEX, "Prata",
                2024, "1HGCM82633A123456", new BigDecimal("120000.00"), null, null);
    }

    private DealerResponseDTO criarDealer(String razaoSocial, String cnpj) {
        return dealerService.criar(new DealerRequestDTO(
                razaoSocial, cnpj,
                new EnderecoDTO("Av. Principal, 100", "Centro", "São Paulo", "SP", "01310100")));
    }

    @Test
    void postDeveCriarVeiculoERetornar201ComLocation() throws Exception {
        mockMvc.perform(post("/vehicles")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(vehicleRequestDTOValido())))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.marca").value("Toyota"))
                .andExpect(jsonPath("$.modelo").value("Corolla"));
    }

    @Test
    void postComCamposObrigatoriosFaltandoDeveRetornar400() throws Exception {
        String vehicleJsonInvalido = """
                {"marca": "", "modelo": null, "cor": "Prata"}
                """;

        mockMvc.perform(post("/vehicles")
                        .contentType("application/json")
                        .content(vehicleJsonInvalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details").isArray());
    }

    @Test
    void getVehiclesDeveListarTodos() throws Exception {
        vehicleService.criar(vehicleRequestDTOValido());
        vehicleService.criar(vehicleRequestDTOValido());

        mockMvc.perform(get("/vehicles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void getVehiclesPorIdDeveRetornarVeiculoExistente() throws Exception {
        VehicleResponseDTO vehicleResponseDTO = vehicleService.criar(vehicleRequestDTOValido());

        mockMvc.perform(get("/vehicles/{vehicleId}", vehicleResponseDTO.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(vehicleResponseDTO.id()))
                .andExpect(jsonPath("$.marca").value("Toyota"));
    }

    @Test
    void getVehiclesPorIdInexistenteDeveRetornar404() throws Exception {
        mockMvc.perform(get("/vehicles/{vehicleId}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void putDeveAtualizarVeiculoExistente() throws Exception {
        VehicleResponseDTO vehicleResponseDTO = vehicleService.criar(vehicleRequestDTOValido());
        VehicleRequestDTO vehicleAtualizacaoRequestDTO = new VehicleRequestDTO(
                "Toyota", "Corolla", FuelType.HIBRIDO, "Preto",
                2025, "1HGCM82633A123456", new BigDecimal("135000.00"), "Bege", null);

        mockMvc.perform(put("/vehicles/{vehicleId}", vehicleResponseDTO.id())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(vehicleAtualizacaoRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cor").value("Preto"))
                .andExpect(jsonPath("$.tipoCombustivel").value("HIBRIDO"));
    }

    @Test
    void putEmIdInexistenteDeveRetornar404() throws Exception {
        mockMvc.perform(put("/vehicles/{vehicleId}", 999L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(vehicleRequestDTOValido())))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteDeveExcluirVeiculoERetornar204() throws Exception {
        VehicleResponseDTO vehicleResponseDTO = vehicleService.criar(vehicleRequestDTOValido());

        mockMvc.perform(delete("/vehicles/{vehicleId}", vehicleResponseDTO.id()))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        mockMvc.perform(get("/vehicles/{vehicleId}", vehicleResponseDTO.id()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteEmIdInexistenteDeveRetornar404() throws Exception {
        mockMvc.perform(delete("/vehicles/{vehicleId}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void patchDealerDeveAssociarConcessionariaAoVeiculo() throws Exception {
        VehicleResponseDTO vehicleResponseDTO = vehicleService.criar(vehicleRequestDTOValido());
        DealerResponseDTO dealerResponseDTO = criarDealer("Honda Sul Ltda", "11222333000144");

        mockMvc.perform(patch("/vehicles/{vehicleId}/dealer", vehicleResponseDTO.id())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new VehicleDealerRequestDTO(dealerResponseDTO.id()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dealerId").value(dealerResponseDTO.id()));
    }

    @Test
    void patchDealerComDealerIdInexistenteDeveRetornar404() throws Exception {
        VehicleResponseDTO vehicleResponseDTO = vehicleService.criar(vehicleRequestDTOValido());

        mockMvc.perform(patch("/vehicles/{vehicleId}/dealer", vehicleResponseDTO.id())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new VehicleDealerRequestDTO(999L))))
                .andExpect(status().isNotFound());
    }
}

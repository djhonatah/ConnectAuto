package com.acc.connectauto.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

import com.acc.connectauto.dto.request.VehicleRequestDTO;
import com.acc.connectauto.dto.response.VehicleResponseDTO;
import com.acc.connectauto.entity.FuelType;
import com.acc.connectauto.service.VehicleService;

import tools.jackson.databind.ObjectMapper;

/**
 * Sobe a aplicação completa (incluindo a camada web real, via MockMvc) e chama os
 * endpoints HTTP de {@link VehicleController} exatamente como um cliente real faria,
 * verificando status HTTP e corpo JSON de cada resposta.
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

    private VehicleRequestDTO requestValido() {
        return new VehicleRequestDTO(
                "Toyota", "Corolla", FuelType.FLEX, "Prata",
                2024, "1HGCM82633A123456", new BigDecimal("120000.00"), null);
    }

    @Test
    void postDeveCriarVeiculoERetornar201ComLocation() throws Exception {
        mockMvc.perform(post("/vehicles")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.marca").value("Toyota"))
                .andExpect(jsonPath("$.modelo").value("Corolla"));
    }

    @Test
    void postComCamposObrigatoriosFaltandoDeveRetornar400() throws Exception {
        String jsonInvalido = """
                {"marca": "", "modelo": null, "cor": "Prata"}
                """;

        mockMvc.perform(post("/vehicles")
                        .contentType("application/json")
                        .content(jsonInvalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details").isArray());
    }

    @Test
    void getVehiclesDeveListarTodos() throws Exception {
        vehicleService.criar(requestValido());
        vehicleService.criar(requestValido());

        mockMvc.perform(get("/vehicles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void getVehiclesPorIdDeveRetornarVeiculoExistente() throws Exception {
        VehicleResponseDTO criado = vehicleService.criar(requestValido());

        mockMvc.perform(get("/vehicles/{id}", criado.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(criado.id()))
                .andExpect(jsonPath("$.marca").value("Toyota"));
    }

    @Test
    void getVehiclesPorIdInexistenteDeveRetornar404() throws Exception {
        mockMvc.perform(get("/vehicles/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void putDeveAtualizarVeiculoExistente() throws Exception {
        VehicleResponseDTO criado = vehicleService.criar(requestValido());
        VehicleRequestDTO atualizacao = new VehicleRequestDTO(
                "Toyota", "Corolla", FuelType.HIBRIDO, "Preto",
                2025, "1HGCM82633A123456", new BigDecimal("135000.00"), "Bege");

        mockMvc.perform(put("/vehicles/{id}", criado.id())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(atualizacao)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cor").value("Preto"))
                .andExpect(jsonPath("$.tipoCombustivel").value("HIBRIDO"));
    }

    @Test
    void putEmIdInexistenteDeveRetornar404() throws Exception {
        mockMvc.perform(put("/vehicles/{id}", 999L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteDeveExcluirVeiculoERetornar204() throws Exception {
        VehicleResponseDTO criado = vehicleService.criar(requestValido());

        mockMvc.perform(delete("/vehicles/{id}", criado.id()))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        mockMvc.perform(get("/vehicles/{id}", criado.id()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteEmIdInexistenteDeveRetornar404() throws Exception {
        mockMvc.perform(delete("/vehicles/{id}", 999L))
                .andExpect(status().isNotFound());
    }
}

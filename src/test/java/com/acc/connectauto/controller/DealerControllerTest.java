package com.acc.connectauto.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import com.acc.connectauto.client.ViaCepClient;
import com.acc.connectauto.dto.EnderecoDTO;
import com.acc.connectauto.dto.ViaCepResponseDTO;
import com.acc.connectauto.dto.request.DealerRequestDTO;
import com.acc.connectauto.dto.request.VehicleRequestDTO;
import com.acc.connectauto.dto.response.DealerResponseDTO;
import com.acc.connectauto.entity.FuelType;
import com.acc.connectauto.service.DealerService;
import com.acc.connectauto.service.VehicleService;

import tools.jackson.databind.ObjectMapper;

/**
 * Testes de integração dos endpoints HTTP de {@link DealerController}, via MockMvc.
 * {@link ViaCepClient} é substituído por um dublê (@MockitoBean) para não depender de
 * uma chamada HTTP real ao ViaCEP.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DealerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ViaCepClient viaCepClient;

    @BeforeEach
    void configurarViaCepClient() {
        when(viaCepClient.buscarEnderecoPorCep(anyString())).thenReturn(
                new ViaCepResponseDTO("01310-100", "Av. Principal", "Centro", "São Paulo", "SP", null));
    }

    @Autowired
    private DealerService dealerService;

    @Autowired
    private VehicleService vehicleService;

    private DealerRequestDTO dealerRequestDTOValido() {
        return new DealerRequestDTO(
                "Auto Center Toyota Ltda",
                "12345678000199",
                new EnderecoDTO("Av. Principal, 100", "Centro", "São Paulo", "SP", "01310100"));
    }

    @Test
    void postDeveCriarDealerERetornar201ComLocation() throws Exception {
        mockMvc.perform(post("/dealer")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dealerRequestDTOValido())))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.razaoSocial").value("Auto Center Toyota Ltda"))
                .andExpect(jsonPath("$.endereco.cidade").value("São Paulo"));
    }

    @Test
    void postComCamposObrigatoriosFaltandoDeveRetornar400() throws Exception {
        String dealerJsonInvalido = """
                {"razaoSocial": "", "cnpj": "123"}
                """;

        mockMvc.perform(post("/dealer")
                        .contentType("application/json")
                        .content(dealerJsonInvalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details").isArray());
    }

    @Test
    void getDealerDeveListarTodos() throws Exception {
        dealerService.criar(dealerRequestDTOValido());
        dealerService.criar(new DealerRequestDTO(
                "Honda Sul Ltda",
                "11222333000144",
                new EnderecoDTO("Rua das Flores, 200", "Batel", "Curitiba", "PR", "80420100")));

        mockMvc.perform(get("/dealer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void getDealerPorIdDeveRetornarDealerExistente() throws Exception {
        DealerResponseDTO dealerResponseDTO = dealerService.criar(dealerRequestDTOValido());

        mockMvc.perform(get("/dealer/{dealerId}", dealerResponseDTO.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(dealerResponseDTO.id()))
                .andExpect(jsonPath("$.razaoSocial").value("Auto Center Toyota Ltda"));
    }

    @Test
    void getDealerPorIdInexistenteDeveRetornar404() throws Exception {
        mockMvc.perform(get("/dealer/{dealerId}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void putDeveAtualizarDealerExistente() throws Exception {
        DealerResponseDTO dealerResponseDTO = dealerService.criar(dealerRequestDTOValido());
        DealerRequestDTO dealerAtualizacaoRequestDTO = new DealerRequestDTO(
                "Auto Center Toyota S.A.",
                "12345678000199",
                new EnderecoDTO("Av. Nova, 500", "Jardins", "Campinas", "SP", "13010000"));

        mockMvc.perform(put("/dealer/{dealerId}", dealerResponseDTO.id())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dealerAtualizacaoRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.razaoSocial").value("Auto Center Toyota S.A."))
                .andExpect(jsonPath("$.endereco.cidade").value("Campinas"));
    }

    @Test
    void putEmIdInexistenteDeveRetornar404() throws Exception {
        mockMvc.perform(put("/dealer/{dealerId}", 999L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dealerRequestDTOValido())))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteDeveExcluirDealerERetornar204() throws Exception {
        DealerResponseDTO dealerResponseDTO = dealerService.criar(dealerRequestDTOValido());

        mockMvc.perform(delete("/dealer/{dealerId}", dealerResponseDTO.id()))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        mockMvc.perform(get("/dealer/{dealerId}", dealerResponseDTO.id()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteEmIdInexistenteDeveRetornar404() throws Exception {
        mockMvc.perform(delete("/dealer/{dealerId}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getVehiclesDeveListarVeiculosDaConcessionaria() throws Exception {
        DealerResponseDTO dealerResponseDTO = dealerService.criar(dealerRequestDTOValido());
        vehicleService.criar(new VehicleRequestDTO(
                "Toyota", "Corolla", FuelType.FLEX, "Prata",
                2024, "1HGCM82633A123456", new BigDecimal("120000.00"), null, dealerResponseDTO.id()));
        vehicleService.criar(new VehicleRequestDTO(
                "Honda", "Civic", FuelType.FLEX, "Branco",
                2024, "9BWZZZ377VT004251", new BigDecimal("130000.00"), null, null));

        mockMvc.perform(get("/dealer/{dealerId}/vehicles", dealerResponseDTO.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].modelo").value("Corolla"));
    }

    @Test
    void getVehiclesEmDealerIdInexistenteDeveRetornar404() throws Exception {
        mockMvc.perform(get("/dealer/{dealerId}/vehicles", 999L))
                .andExpect(status().isNotFound());
    }
}

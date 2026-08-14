package com.acc.connectauto.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
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
import com.acc.connectauto.service.DealerService;
import com.acc.connectauto.service.VehicleService;

import tools.jackson.databind.ObjectMapper;

/**
 * Testes de integração dos endpoints HTTP de {@link VehicleController}, via
 * MockMvc.
 * {@link ViaCepClient} é substituído por um dublê (@MockitoBean) — os testes de
 * Vehicle
 * criam Dealers só como apoio, e não devem depender de uma chamada HTTP real ao
 * ViaCEP.
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

        @MockitoBean
        private ViaCepClient viaCepClient;

        @BeforeEach
        void configurarViaCepClient() {
                when(viaCepClient.buscarEnderecoPorCep(anyString()))
                                .thenAnswer(invocation -> new ViaCepResponseDTO("01310-100", "Av. Principal", "Centro",
                                                "São Paulo", "SP", null));
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
        void postComTipoCombustivelInvalidoDeveRetornar400() throws Exception {
                String vehicleJsonComEnumInvalido = """
                                {"marca": "Toyota", "modelo": "Corolla", "tipoCombustivel": "NUCLEAR",
                                 "cor": "Prata", "ano": 2024, "chassi": "1HGCM82633A123456", "valor": 120000.00}
                                """;

                mockMvc.perform(post("/vehicles")
                                .contentType("application/json")
                                .content(vehicleJsonComEnumInvalido))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value(400))
                                .andExpect(jsonPath("$.message").value(containsString("NUCLEAR")))
                                .andExpect(jsonPath("$.message").value(containsString("GASOLINA")))
                                .andExpect(jsonPath("$.message").value(not(containsString("com.acc.connectauto"))));
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
                vehicleService.criar(vehicleRequestDTOValido("1HGCM82633A123456"));
                vehicleService.criar(vehicleRequestDTOValido("9BWZZZ377VT004251"));

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
                DealerResponseDTO dealerResponseDTO = criarDealer("Honda Sul Ltda", "11222333000181");

                mockMvc.perform(patch("/vehicles/{vehicleId}/dealer", vehicleResponseDTO.id())
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(
                                                new VehicleDealerRequestDTO(dealerResponseDTO.id()))))
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

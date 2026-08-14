package com.acc.connectauto.controller;

import static org.hamcrest.Matchers.hasSize;
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

        // Resposta varia por CEP para que os testes que criam dealers em cidades
        // diferentes
        // continuem representativos agora que o endereço é sobrescrito pelo retorno do
        // ViaCEP.
        @BeforeEach
        void configurarViaCepClient() {
                when(viaCepClient.buscarEnderecoPorCep(anyString())).thenAnswer(invocation -> {
                        String cep = invocation.getArgument(0);
                        return switch (cep) {
                                case "80420100" -> new ViaCepResponseDTO("80420-100", "Rua das Flores", "Batel",
                                                "Curitiba", "PR", null);
                                case "13010000" -> new ViaCepResponseDTO("13010-000", "Av. Nova", "Jardins", "Campinas",
                                                "SP", null);
                                default -> new ViaCepResponseDTO("01310-100", "Av. Principal", "Centro", "São Paulo",
                                                "SP", null);
                        };
                });
        }

        @Autowired
        private DealerService dealerService;

        @Autowired
        private VehicleService vehicleService;

        private DealerRequestDTO dealerRequestDTOValido() {
                return new DealerRequestDTO(
                                "Auto Center Toyota Ltda",
                                "12345678000195",
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
                                "11222333000181",
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
                                "12345678000195",
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

        @Test
        void postComCepInexistenteDeveRetornar400() throws Exception {
                when(viaCepClient.buscarEnderecoPorCep("00000000"))
                                .thenReturn(new ViaCepResponseDTO(null, null, null, null, null, true));

                DealerRequestDTO dealerComCepInexistenteRequestDTO = new DealerRequestDTO(
                                "Auto Center Toyota Ltda",
                                "12345678000195",
                                new EnderecoDTO("Av. Principal, 100", "Centro", "São Paulo", "SP", "00000000"));

                mockMvc.perform(post("/dealer")
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(dealerComCepInexistenteRequestDTO)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.message").value("CEP 00000000 não encontrado"));
        }

        @Test
        void postComCnpjComDigitoVerificadorInvalidoDeveRetornar400() throws Exception {
                DealerRequestDTO dealerComCnpjInvalidoRequestDTO = new DealerRequestDTO(
                                "Auto Center Toyota Ltda",
                                "12345678000199",
                                new EnderecoDTO("Av. Principal, 100", "Centro", "São Paulo", "SP", "01310100"));

                mockMvc.perform(post("/dealer")
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(dealerComCnpjInvalidoRequestDTO)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.details").isArray());
        }

        @Test
        void postComCepForaDoFormatoDeveRetornar400() throws Exception {
                DealerRequestDTO dealerComCepForaDoFormatoRequestDTO = new DealerRequestDTO(
                                "Auto Center Toyota Ltda",
                                "12345678000195",
                                new EnderecoDTO("Av. Principal, 100", "Centro", "São Paulo", "SP", "ABCDE-123"));

                mockMvc.perform(post("/dealer")
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(dealerComCepForaDoFormatoRequestDTO)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.details").isArray());
        }

        @Test
        void postComCnpjJaCadastradoDeveRetornar409() throws Exception {
                dealerService.criar(dealerRequestDTOValido());

                DealerRequestDTO dealerComCnpjDuplicadoRequestDTO = new DealerRequestDTO(
                                "Outra Razão Social Ltda",
                                "12345678000195",
                                new EnderecoDTO("Rua das Flores, 200", "Batel", "Curitiba", "PR", "80420100"));

                mockMvc.perform(post("/dealer")
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(dealerComCnpjDuplicadoRequestDTO)))
                                .andExpect(status().isConflict());
        }

    @Test
    void deleteDealerComVeiculoAssociadoDeveRetornar409() throws Exception {
        // Fluxo inteiro via MockMvc (criar dealer, criar veículo, excluir), não misturado
        // com chamadas diretas ao service: dentro de um teste @Transactional, misturar os
        // dois jeitos de acesso pode usar EntityManagers diferentes (OSIV por requisição
        // do MockMvc vs. o EntityManager da transação do próprio teste) e gerar um erro de
        // estado de entidade que não existe de verdade em produção.
        String dealerResponseJson = mockMvc.perform(post("/dealer")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dealerRequestDTOValido())))
                        .andReturn().getResponse().getContentAsString();
        Long dealerId = objectMapper.readTree(dealerResponseJson).get("id").asLong();

        VehicleRequestDTO vehicleComDealerRequestDTO = new VehicleRequestDTO(
                "Toyota", "Corolla", FuelType.FLEX, "Prata",
                2024, "1HGCM82633A123456", new BigDecimal("120000.00"), null, dealerId);
        mockMvc.perform(post("/vehicles")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(vehicleComDealerRequestDTO)))
                        .andExpect(status().isCreated());

        mockMvc.perform(delete("/dealer/{dealerId}", dealerId))
                        .andExpect(status().isConflict());
    }

        @Test
        void getComVehicleIdEmFormatoInvalidoDeveRetornar400() throws Exception {
                mockMvc.perform(get("/dealer/{dealerId}", "abc"))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void verboHttpNaoSuportadoDeveRetornar405() throws Exception {
                mockMvc.perform(patch("/dealer/{dealerId}", 1L))
                                .andExpect(status().isMethodNotAllowed());
        }
}

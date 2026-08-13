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

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.acc.connectauto.dto.EnderecoDTO;
import com.acc.connectauto.dto.request.DealerRequestDTO;
import com.acc.connectauto.dto.response.DealerResponseDTO;
import com.acc.connectauto.service.DealerService;

import tools.jackson.databind.ObjectMapper;

/**
 * Testes de integração dos endpoints HTTP de {@link DealerController}, via MockMvc.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DealerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DealerService dealerService;

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
}

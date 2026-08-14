package com.acc.connectauto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.acc.connectauto.client.ViaCepClient;
import com.acc.connectauto.dto.EnderecoDTO;
import com.acc.connectauto.dto.ViaCepResponseDTO;
import com.acc.connectauto.dto.request.DealerRequestDTO;
import com.acc.connectauto.dto.request.VehicleRequestDTO;
import com.acc.connectauto.entity.FuelType;
import com.acc.connectauto.service.DealerService;

import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ExtendWith(OutputCaptureExtension.class)
class LoggingTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DealerService dealerService;

    @MockitoBean
    private ViaCepClient viaCepClient;

    private DealerRequestDTO dealerRequestDTOValido() {
        return new DealerRequestDTO(
                "Auto Center Toyota Ltda",
                "12345678000195",
                new EnderecoDTO("Av. Principal, 100", "Centro", "São Paulo", "SP", "01310100"));
    }

    private VehicleRequestDTO vehicleRequestDTOValido() {
        return new VehicleRequestDTO(
                "Toyota", "Corolla", FuelType.FLEX, "Prata",
                2024, "1HGCM82633A123456", new BigDecimal("120000.00"), null, null);
    }

    @Test
    void criarVeiculoDeveGerarLogNoControllerENoService(CapturedOutput capturedOutput) throws Exception {
        mockMvc.perform(post("/vehicles")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(vehicleRequestDTOValido())))
                .andExpect(status().isCreated());

        assertThat(capturedOutput).contains("POST /vehicles - criando veículo: marca=Toyota, modelo=Corolla");
        assertThat(capturedOutput).contains("Veículo criado: id=");
    }

    @Test
    void atualizarEExcluirVeiculoDevemGerarLog(CapturedOutput capturedOutput) throws Exception {
        String vehicleJson = objectMapper.writeValueAsString(vehicleRequestDTOValido());
        Long vehicleId = objectMapper.readTree(
                mockMvc.perform(post("/vehicles").contentType("application/json").content(vehicleJson))
                        .andReturn().getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(put("/vehicles/{vehicleId}", vehicleId)
                        .contentType("application/json")
                        .content(vehicleJson))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/vehicles/{vehicleId}", vehicleId))
                .andExpect(status().isNoContent());

        assertThat(capturedOutput).contains("PUT /vehicles/" + vehicleId + " - atualizando veículo");
        assertThat(capturedOutput).contains("Veículo atualizado: id=" + vehicleId);
        assertThat(capturedOutput).contains("DELETE /vehicles/" + vehicleId + " - excluindo veículo");
        assertThat(capturedOutput).contains("Veículo excluído: id=" + vehicleId);
    }

    @Test
    void criarConcessionariaDeveGerarLogNoControllerENoService(CapturedOutput capturedOutput) throws Exception {
        when(viaCepClient.buscarEnderecoPorCep(anyString())).thenReturn(
                new ViaCepResponseDTO("01310-100", "Av. Principal", "Centro", "São Paulo", "SP", null));

        mockMvc.perform(post("/dealer")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dealerRequestDTOValido())))
                .andExpect(status().isCreated());

        assertThat(capturedOutput).contains("POST /dealer - criando concessionária: razaoSocial=Auto Center Toyota Ltda");
        assertThat(capturedOutput).contains("Concessionária criada: id=");
    }

    @Test
    void excluirConcessionariaDeveGerarLog(CapturedOutput capturedOutput) {
        when(viaCepClient.buscarEnderecoPorCep(anyString())).thenReturn(
                new ViaCepResponseDTO("01310-100", "Av. Principal", "Centro", "São Paulo", "SP", null));

        Long dealerId = dealerService.criar(dealerRequestDTOValido()).id();
        dealerService.excluir(dealerId);

        assertThat(capturedOutput).contains("Concessionária excluída: id=" + dealerId);
    }

    @Test
    void veiculoInexistenteDeveGerarLogDeWarn(CapturedOutput capturedOutput) throws Exception {
        mockMvc.perform(get("/vehicles/{vehicleId}", 999L))
                .andExpect(status().isNotFound());

        assertThat(capturedOutput).contains("Recurso não encontrado: Veículo não encontrado com id 999");
    }

    @Test
    void validacaoInvalidaDeveGerarLogDeWarn(CapturedOutput capturedOutput) throws Exception {
        String vehicleJsonInvalido = """
                {"marca": "", "modelo": null, "cor": "Prata"}
                """;

        mockMvc.perform(post("/vehicles")
                        .contentType("application/json")
                        .content(vehicleJsonInvalido))
                .andExpect(status().isBadRequest());

        assertThat(capturedOutput).contains("Erro de validação:");
    }
}

package com.acc.connectauto.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.acc.connectauto.dto.ViaCepResponseDTO;

/**
 * Testa {@link ViaCepClient} isoladamente: MockRestServiceServer intercepta a chamada HTTP
 * antes dela sair para a rede, então este teste não depende de internet nem do ViaCEP real.
 */
class ViaCepClientTest {

    @Test
    void deveBuscarEnderecoParaCepValido() {
        RestClient.Builder restClientBuilder = RestClient.builder();
        MockRestServiceServer mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();

        mockServer.expect(requestTo("https://viacep.com.br/ws/01310100/json/"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                            "cep": "01310-100",
                            "logradouro": "Avenida Paulista",
                            "complemento": "",
                            "bairro": "Bela Vista",
                            "localidade": "São Paulo",
                            "uf": "SP",
                            "ibge": "3550308"
                        }
                        """, MediaType.APPLICATION_JSON));

        ViaCepClient viaCepClient = new ViaCepClient(restClientBuilder);
        ViaCepResponseDTO viaCepResponseDTO = viaCepClient.buscarEnderecoPorCep("01310100");

        assertThat(viaCepResponseDTO.logradouro()).isEqualTo("Avenida Paulista");
        assertThat(viaCepResponseDTO.bairro()).isEqualTo("Bela Vista");
        assertThat(viaCepResponseDTO.localidade()).isEqualTo("São Paulo");
        assertThat(viaCepResponseDTO.uf()).isEqualTo("SP");
        assertThat(viaCepResponseDTO.erro()).isNull();

        mockServer.verify();
    }

    @Test
    void deveRetornarErroParaCepInexistente() {
        RestClient.Builder restClientBuilder = RestClient.builder();
        MockRestServiceServer mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();

        // O ViaCEP não devolve 404 para CEP inexistente: devolve 200 com {"erro": true}.
        mockServer.expect(requestTo("https://viacep.com.br/ws/00000000/json/"))
                .andRespond(withSuccess("""
                        {"erro": true}
                        """, MediaType.APPLICATION_JSON));

        ViaCepClient viaCepClient = new ViaCepClient(restClientBuilder);
        ViaCepResponseDTO viaCepResponseDTO = viaCepClient.buscarEnderecoPorCep("00000000");

        assertThat(viaCepResponseDTO.erro()).isTrue();
        assertThat(viaCepResponseDTO.logradouro()).isNull();

        mockServer.verify();
    }
}

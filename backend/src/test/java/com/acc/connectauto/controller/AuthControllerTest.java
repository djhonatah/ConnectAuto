package com.acc.connectauto.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${connectauto.security.admin-email}")
    private String adminEmail;

    @Value("${connectauto.security.admin-password}")
    private String adminPassword;

    @Test
    void loginComCredenciaisValidasDeveRetornarToken() throws Exception {
        String corpo = objectMapper.writeValueAsString(new LoginRequest(adminEmail, adminPassword));

        mockMvc.perform(post("/auth/login").contentType("application/json").content(corpo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.email").value(adminEmail));
    }

    @Test
    void loginComSenhaErradaDeveRetornar401() throws Exception {
        String corpo = objectMapper.writeValueAsString(new LoginRequest(adminEmail, "senha-errada"));

        mockMvc.perform(post("/auth/login").contentType("application/json").content(corpo))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("E-mail ou senha inválidos."));
    }

    @Test
    void loginComEmailInexistenteDeveRetornar401() throws Exception {
        String corpo = objectMapper.writeValueAsString(new LoginRequest("ninguem@connectauto.com.br", "qualquer"));

        mockMvc.perform(post("/auth/login").contentType("application/json").content(corpo))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void endpointProtegidoSemTokenDeveRetornar401() throws Exception {
        mockMvc.perform(get("/vehicles"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void endpointProtegidoComTokenValidoDeveRetornar200() throws Exception {
        String corpoLogin = objectMapper.writeValueAsString(new LoginRequest(adminEmail, adminPassword));
        String respostaLogin = mockMvc.perform(post("/auth/login").contentType("application/json").content(corpoLogin))
                .andReturn().getResponse().getContentAsString();
        String token = objectMapper.readTree(respostaLogin).get("token").asString();

        mockMvc.perform(get("/vehicles").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void endpointProtegidoComTokenInvalidoDeveRetornar401() throws Exception {
        mockMvc.perform(get("/vehicles").header("Authorization", "Bearer token-forjado-invalido"))
                .andExpect(status().isUnauthorized());
    }

    private record LoginRequest(String email, String senha) {
    }
}

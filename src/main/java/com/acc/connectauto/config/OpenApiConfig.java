package com.acc.connectauto.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

/**
 * Basic SpringDoc/OpenAPI metadata, exposed at {@code /swagger-ui.html} and
 * {@code /v3/api-docs}.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI connectAutoOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("ConnectAuto API")
                        .description("API do sistema ConnectAuto")
                        .version("v0.0.1"));
    }
}

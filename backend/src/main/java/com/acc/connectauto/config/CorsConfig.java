package com.acc.connectauto.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Libera o navegador a chamar a API a partir das origens do frontend
 * (ex.: http://localhost:5173 em desenvolvimento). Sem isso, o backend
 * responde normalmente a curl/Postman, mas o navegador bloqueia a
 * chamada por política de CORS.
 *
 * As origens permitidas vêm de application.properties
 * (connectauto.cors.allowed-origins), para não precisar recompilar ao
 * trocar de ambiente.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${connectauto.cors.allowed-origins}")
    private String[] allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}

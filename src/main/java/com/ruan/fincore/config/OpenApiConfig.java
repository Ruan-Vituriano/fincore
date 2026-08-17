package com.ruan.fincore.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_FORMAT = "JWT";
    private static final String SECURITY_SCHEME_NAME = "Bearer Token";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Fincore API")
                        .description("API REST para gestão de finanças pessoais com sugestões via IA")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Ruan Vituriano")
                                .email("ruan@example.com")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat(BEARER_FORMAT)
                                        .description("Insira o token JWT obtido no login")));
    }
}

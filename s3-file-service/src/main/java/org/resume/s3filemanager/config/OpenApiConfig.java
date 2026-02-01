package org.resume.s3filemanager.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация OpenAPI/Swagger для документации REST API.
 * <p>
 * Настраивает метаинформацию API: название, версию, описание и контакты.
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "Bearer Authentication";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CloudFileHub API")
                        .version("1.0")
                        .description("Monorepo для платформы управления файлами с S3 хранилищем и антивирусной проверкой")
                        .contact(new Contact()
                                .name("Vladimir")
                                .url("https://github.com/Vldr22/CloudFileHub")))
                .addSecurityItem((new SecurityRequirement().addList(SECURITY_SCHEME_NAME)))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }

}

package org.resume.s3filemanager.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Getter
@Validated
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "app.swagger")
public class SwaggerProperties {

    @NotEmpty(message = "Swagger servers list must not be empty")
    private final List<ServerProperties> servers;

    @Getter
    @RequiredArgsConstructor
    public static class ServerProperties {

        @NotBlank(message = "Server url is required")
        private final String url;

        @NotBlank(message = "Server description is required")
        private final String description;
    }
}
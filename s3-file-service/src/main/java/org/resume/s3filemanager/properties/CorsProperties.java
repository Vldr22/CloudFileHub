package org.resume.s3filemanager.properties;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Getter
@Validated
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "app.cors")
public class CorsProperties {

    @NotEmpty(message = "At least one allowed origin is required")
    private final List<String> allowedOrigins;

    private final List<String> allowedMethods;

    private final List<String> allowedHeaders;

}

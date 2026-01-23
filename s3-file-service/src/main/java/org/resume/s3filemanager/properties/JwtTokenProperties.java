package org.resume.s3filemanager.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Validated
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "app.jwt")
public class JwtTokenProperties {

    @NotBlank(message = "JWT secret key is required")
    private final String secretKey;

    @Positive(message = "JWT expiration must be positive")
    private final long expiration;

}

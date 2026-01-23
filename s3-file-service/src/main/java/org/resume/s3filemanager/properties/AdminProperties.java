package org.resume.s3filemanager.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "app.admin")
public class AdminProperties {

    @NotBlank(message = "Admin name is required")
    private final String name;

    @NotBlank(message = "Admin password is required")
    private final String password;
}

package org.resume.fileantivirusservice.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Component
@ConfigurationProperties(prefix = "clamav")
public class ClamAVProperties {

    @NotBlank(message = "ClamAV host cannot be empty")
    private String host;

    @NotNull(message = "ClamAV port cannot be null")
    private Integer port;

}

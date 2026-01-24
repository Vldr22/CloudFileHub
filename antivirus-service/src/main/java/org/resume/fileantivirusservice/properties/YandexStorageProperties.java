package org.resume.fileantivirusservice.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Data
@Validated
@Component
@ConfigurationProperties(prefix = "yandex.storage")
public class YandexStorageProperties {

    @NotBlank(message = "Access key is required")
    private String accessKey;

    @NotBlank(message = "Secret key is required")
    private String secretKey;

    @NotBlank(message = "Endpoint is required")
    private String endpoint;

    @NotBlank(message = "Region is required")
    private String region;

    @NotBlank(message = "BucketName is required")
    private String bucketName;

}

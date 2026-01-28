package org.resume.common.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * Конфигурация для подключения к Yandex Object Storage (S3-совместимое API).
 * <p>
 * Свойства загружаются из application.yml с префиксом "yandex.storage".
 */

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

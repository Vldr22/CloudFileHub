package org.resume.common.properties;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * Конфигурация Kafka топиков и retry политики.
 */
@Data
@Validated
@Component
@ConfigurationProperties(prefix = "kafka")
public class KafkaProperties {

    private KafkaTopic topic;
    private RetryConfig retry;

    @Data
    public static class KafkaTopic {
        @NotBlank(message = "File upload events topic is required")
        private String fileUploadEvents;

        @NotBlank(message = "File scan results topic is required")
        private String fileScanResults;
    }

    @Data
    public static class RetryConfig {
        @Min(value = 1, message = "Max attempts must be at least 1")
        private Integer maxAttempts;

        @Min(value = 1000, message = "Backoff delay must be at least 1000ms")
        private Long backoffDelay;
    }

}

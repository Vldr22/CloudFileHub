package org.resume.fileantivirusservice.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.TopicPartition;
import org.resume.common.model.FileUploadEvent;
import org.resume.common.properties.KafkaProperties;
import org.resume.fileantivirusservice.constant.ErrorMessages;
import org.resume.fileantivirusservice.producer.FileScanResultProducer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;

/**
 * Конфигурация Kafka retry и Dead Letter Topic.
 * <p>
 * Стратегия обработки ошибок:
 * <ul>
 *   <li>N попыток с exponential backoff</li>
 *   <li>После исчерпания попыток — отправка в DLT</li>
 *   <li>Логирование каждой retry попытки</li>
 * </ul>
 * <p>
 * @see KafkaProperties
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(KafkaProperties.class)
public class KafkaRetryConfig {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaProperties kafkaProperties;
    private final FileScanResultProducer fileScanResultProducer;

    /**
     * Создаёт {@link DefaultErrorHandler} с exponential backoff и DLT.
     *
     * @return настроенный error handler
     */
    @Bean
    public DefaultErrorHandler errorHandler() {
        ExponentialBackOffWithMaxRetries backOff = createBackOff();
        DeadLetterPublishingRecoverer recoverer = createRecoverer();

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);
        configureRetryListener(errorHandler);

        return errorHandler;
    }

    /**
     * Создаёт BackOff стратегию с exponential увеличением задержки.
     */
    private ExponentialBackOffWithMaxRetries createBackOff() {
        KafkaProperties.RetryConfig retry = kafkaProperties.getRetry();

        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(
                retry.getMaxAttempts()
        );
        backOff.setInitialInterval(retry.getBackoffDelay());
        backOff.setMultiplier(retry.getMultiplier());
        backOff.setMaxInterval(retry.getMaxInterval());

        return backOff;
    }

    /**
     * Создаёт recoverer для отправки failed сообщений в DLT.
     * <p>
     * Сохраняет оригинальный partition для консистенции.
     *
     * @return настроенный recoverer
     */
    private DeadLetterPublishingRecoverer createRecoverer() {
        return new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, e) -> {
                    log.error("Sending to DLT after {} attempts: topic={}, key={}, error={}",
                            kafkaProperties.getRetry().getMaxAttempts(),
                            record.topic(),
                            record.key(),
                            e.getMessage());

                    sendErrorStatus(record.value());

                    return new TopicPartition(
                            kafkaProperties.getTopics().getFileUploadEventsDlt(),
                            record.partition()
                    );
                }
        );
    }

    /**
     * Отправляет ERROR статус в file-scan-results перед отправкой в DLT.
     */
    private void sendErrorStatus(Object recordValue) {
        try {
            if (recordValue instanceof FileUploadEvent event) {
                fileScanResultProducer.sendErrorScanResult(
                        event.getS3Key(),
                        ErrorMessages.DLQ_SCAN_FAILED
                );
                log.info("Error status sent for file: s3Key={}", event.getS3Key());
            }
        } catch (Exception e) {
            log.error("Failed to send error status: {}", e.getMessage());
        }
    }

    /**
     * Настраивает логирование retry попыток.
     * <p>
     * Логирует только попытки до достижения лимита,
     * последняя попытка логируется в {@link #createRecoverer()}.
     *
     * @param errorHandler error handler для настройки
     */
    private void configureRetryListener(DefaultErrorHandler errorHandler) {
        int maxAttempts = kafkaProperties.getRetry().getMaxAttempts();

        errorHandler.setRetryListeners((record, ex, deliveryAttempt) -> {
            if (deliveryAttempt <= maxAttempts) {
                log.warn("Retry attempt {}/{}: topic={}, key={}, error={}",
                        deliveryAttempt,
                        maxAttempts,
                        record.topic(),
                        record.key(),
                        ex.getMessage());
            }
        });
    }

}

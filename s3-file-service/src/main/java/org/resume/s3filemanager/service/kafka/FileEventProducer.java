package org.resume.s3filemanager.service.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.resume.common.model.FileUploadEvent;
import org.resume.common.properties.KafkaProperties;
import org.resume.s3filemanager.exception.KafkaSendException;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Producer для отправки событий загрузки файлов в Kafka.
 * <p>
 * Использует синхронную отправку с таймаутом для гарантии доставки.
 * Вызывается только через OutboxService.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@EnableConfigurationProperties(KafkaProperties.class)
public class FileEventProducer {

    private static final int SEND_TIMEOUT_SECONDS = 10;

    private final KafkaTemplate<String, FileUploadEvent> kafkaTemplate;
    private final KafkaProperties kafkaProperties;

    public void sendFileUploadEvent(FileUploadEvent event) {
        String topic = kafkaProperties.getTopics().getFileUploadEvents();

        log.info("Sending file upload event to Kafka: fileId={}, s3Key={}",
                event.getFileId(), event.getS3Key());

        try {
            kafkaTemplate.send(topic, event.getFileId().toString(), event)
                    .get(SEND_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            log.debug("File upload event sent successfully: fileId={}", event.getFileId());

        } catch (ExecutionException e) {
            log.error("Failed to send event to Kafka: fileId={}", event.getFileId(), e);
            throw new KafkaSendException(e, event.getFileId());
        } catch (TimeoutException e) {
            log.error("Kafka send timed out: fileId={}", event.getFileId(), e);
            throw new KafkaSendException(e, event.getFileId());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Kafka send interrupted: fileId={}", event.getFileId(), e);
            throw new KafkaSendException(e, event.getFileId());
        }
    }
}

package org.resume.s3filemanager.service.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.resume.common.model.FileUploadEvent;
import org.resume.common.properties.YandexStorageProperties;
import org.resume.s3filemanager.entity.FileMetadata;
import org.resume.s3filemanager.entity.OutboxEvent;
import org.resume.s3filemanager.enums.OutboxEventType;
import org.resume.s3filemanager.enums.OutboxStatus;
import org.resume.s3filemanager.exception.OutboxSerializationException;
import org.resume.s3filemanager.properties.OutboxProperties;
import org.resume.s3filemanager.repository.OutboxRepository;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * Сервис для работы с Outbox таблицей.
 * <p>
 * Сохраняет события в рамках транзакции загрузки файла.
 * Scheduler периодически вызывает processPendingEvents для доставки в Kafka.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@EnableConfigurationProperties({OutboxProperties.class, YandexStorageProperties.class})
public class OutboxService {

    private final OutboxRepository outboxRepository;
    private final OutboxEventProcessor outboxEventProcessor;
    private final ObjectMapper objectMapper;
    private final OutboxProperties outboxProperties;
    private final YandexStorageProperties yandexStorageProperties;

    public void saveFileUploadEvent(FileMetadata metadata, Long userId) {
        try {
            FileUploadEvent event = FileUploadEvent.builder()
                    .fileId(metadata.getId())
                    .userId(userId)
                    .s3Key(metadata.getUniqueName())
                    .bucketName(yandexStorageProperties.getBucketName())
                    .originalFileName(metadata.getOriginalName())
                    .build();

            String payload = objectMapper.writeValueAsString(event);
            outboxRepository.save(new OutboxEvent(OutboxEventType.FILE_UPLOAD, payload));
            log.debug("Outbox event saved: fileId={}", metadata.getId());

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize outbox event: fileId={}", metadata.getId(), e);
            throw new OutboxSerializationException(e, metadata.getId());
        }
    }

    public void processPendingEvents() {
        List<OutboxEvent> events = outboxRepository.findPendingEvents(
                OutboxStatus.PENDING,
                Instant.now(),
                PageRequest.of(0, outboxProperties.getBatchSize())
        );

        if (events.isEmpty()) {
            return;
        }

        log.debug("Processing {} pending outbox events", events.size());

        for (OutboxEvent event : events) {
            outboxEventProcessor.process(event);
        }
    }
}
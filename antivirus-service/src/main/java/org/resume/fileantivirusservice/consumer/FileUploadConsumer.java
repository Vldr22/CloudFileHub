package org.resume.fileantivirusservice.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.resume.common.model.FileUploadEvent;
import org.resume.fileantivirusservice.config.KafkaRetryConfig;
import org.resume.fileantivirusservice.service.FileOrchestrationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumer для обработки событий загрузки файлов из Kafka.
 * <p>
 * Retry и DLT настроены в {@link KafkaRetryConfig}.
 * При ошибке Spring Kafka автоматически делает retry с exponential backoff,
 * после исчерпания попыток отправляет в DLT.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FileUploadConsumer {

    private final FileOrchestrationService fileOrchestrationService;

    @KafkaListener(
            topics = "${kafka.topics.file-upload-events}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeFileUploadEvent(FileUploadEvent event) {
        log.info("Received file upload event: fileId={}, s3Key={}, userId={}",
                event.getFileId(), event.getS3Key(), event.getUserId());

        fileOrchestrationService.processFileUpload(event);
    }
}

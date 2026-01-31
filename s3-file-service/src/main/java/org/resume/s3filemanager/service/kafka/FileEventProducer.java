package org.resume.s3filemanager.service.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.resume.common.model.FileUploadEvent;
import org.resume.common.properties.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Producer для отправки событий загрузки файлов в Kafka.
 * <p>
 * Отправляет события в Topics file-upload-events для последующей
 * обработки антивирусным сервисом.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@EnableConfigurationProperties(KafkaProperties.class)
public class FileEventProducer {

    private final KafkaTemplate<String, FileUploadEvent> kafkaTemplate;
    private final KafkaProperties kafkaProperties;

    public void sendFileUploadEvent(FileUploadEvent event) {
        String topic = kafkaProperties.getTopics().getFileUploadEvents();

        log.info("Sending file upload event to Kafka: fileId={}, s3Key={}",
                event.getFileId(), event.getS3Key());

        kafkaTemplate.send(topic, event.getFileId().toString(), event)
                .whenComplete((result, e) -> {
                    if (e != null) {
                        log.error("Failed to send file upload event: fileId={}, s3Key={}",
                                event.getFileId(), event.getS3Key(), e);
                    } else {
                        log.debug("File upload event sent successfully: fileId={}, offset={}",
                                event.getFileId(), result.getRecordMetadata().offset());
                    }
                });
    }
}

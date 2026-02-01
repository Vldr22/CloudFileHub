package org.resume.s3filemanager.service.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.resume.common.model.FileUploadEvent;
import org.resume.common.model.ScanStatus;
import org.resume.common.properties.KafkaProperties;
import org.resume.s3filemanager.entity.FileMetadata;
import org.resume.s3filemanager.repository.FileMetadataRepository;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;


/**
 * Сервис для повторной обработки сообщений из Dead Letter Topic.
 * <p>
 * Читает сообщения из DLT и переотправляет в основной топик
 * только если файл всё ещё в статусе ERROR.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RetryDLTService {

    private static final String DLT_REPLAY_GROUP = "dlt-replay";
    private static final Duration POLL_TIMEOUT = Duration.ofSeconds(2);

    private final ConsumerFactory<String, FileUploadEvent> consumerFactory;
    private final KafkaProperties kafkaProperties;
    private final FileEventProducer fileEventProducer;
    private final FileMetadataRepository fileMetadataRepository;

    /**
     * Переотправляет сообщения из DLT в Topic file-upload-events.
     * Retry выполняется только для файлов со scan_status ERROR.
     *
     * @return количество переотправленных сообщений
     */
    public int retryFailedEvents() {
        int retried = 0;
        int skipped = 0;

        try (Consumer<String, FileUploadEvent> consumer = createDltConsumer()) {
            consumer.subscribe(List.of(kafkaProperties.getTopics().getFileUploadEventsDlt()));
            consumer.poll(POLL_TIMEOUT);
            consumer.assignment().forEach(tp -> consumer.seekToBeginning(List.of(tp)));

            while (true) {
                ConsumerRecords<String, FileUploadEvent> records = consumer.poll(POLL_TIMEOUT);
                if (records.isEmpty()) break;

                for (ConsumerRecord<String, FileUploadEvent> record : records) {
                    if (shouldRetry(record.value())) {
                        fileEventProducer.sendFileUploadEvent(record.value());
                        retried++;
                    } else {
                        skipped++;
                    }
                }
            }
        }

        log.info("DLT retry completed: retried={}, skipped={}", retried, skipped);
        return retried;
    }

    private boolean shouldRetry(FileUploadEvent event) {
        Optional<FileMetadata> file = fileMetadataRepository.findByUniqueName(event.getS3Key());
        return file.map(f -> f.getScanStatus() == ScanStatus.ERROR).orElse(false);
    }

    private Consumer<String, FileUploadEvent> createDltConsumer() {
        return consumerFactory.createConsumer(DLT_REPLAY_GROUP, null);
    }

}

package org.resume.fileantivirusservice.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.resume.common.model.FileScanResult;
import org.resume.common.model.ScanStatus;
import org.resume.common.properties.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Producer для отправки результатов сканирования файлов в Kafka.
 * <p>
 * Отправляет результаты в топик file-scan-results для последующей
 * обработки в s3-file-service (обновление статуса файла в БД).
 */
@Slf4j
@Service
@RequiredArgsConstructor
@EnableConfigurationProperties(KafkaProperties.class)
public class FileScanResultProducer {

    private final KafkaTemplate<String, FileScanResult> kafkaTemplate;
    private final KafkaProperties kafkaProperties;

    /**
     * Отправляет результат сканирования файла в Kafka.
     * <p>
     * Использует асинхронную отправку с обработкой результата.
     *
     * @param scanResult результат сканирования файла
     */
    public void sendScanResult(FileScanResult scanResult) {
        String topic = kafkaProperties.getTopics().getFileScanResults();

        log.info("Sending scan result to Kafka: s3Key={}, status={}",
                scanResult.getS3Key(), scanResult.getStatus());

        kafkaTemplate.send(topic, scanResult.getS3Key(), scanResult)
                .whenComplete((result, e) -> {
                    if (e != null) {
                        log.error("Failed to send scan result: s3Key={}, status={}",
                                scanResult.getS3Key(), scanResult.getStatus(), e);
                    } else {
                        log.debug("Scan result sent successfully: s3Key={}, offset={}",
                                scanResult.getS3Key(), result.getRecordMetadata().offset());
                    }
                });
    }

    /**
     * Отправляет результат с ошибкой сканирования в Kafka.
     * <p>
     * Используется когда сканирование не удалось после всех retry попыток.
     *
     * @param s3Key ключ файла в S3
     * @param errorMessage сообщение об ошибке
     */
    public void sendErrorScanResult(String s3Key, String errorMessage) {
        FileScanResult errorResult = FileScanResult.builder()
                .s3Key(s3Key)
                .status(ScanStatus.ERROR)
                .errorMessage(errorMessage)
                .scannedAt(Instant.now())
                .build();

        sendScanResult(errorResult);
    }

}

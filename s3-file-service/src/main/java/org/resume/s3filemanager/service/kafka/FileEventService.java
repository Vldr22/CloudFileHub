package org.resume.s3filemanager.service.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.resume.common.dto.FileUploadEvent;
import org.resume.common.properties.YandexStorageProperties;
import org.resume.s3filemanager.entity.FileMetadata;
import org.springframework.stereotype.Service;

/**
 * Сервис для публикации событий о файлах в Kafka.
 * <p>
 * Конвертирует entity в DTO и отправляет через producer.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileEventService {

    private final FileEventProducer fileEventProducer;
    private final YandexStorageProperties yandexStorageProperties;

    /**
     * Публикует событие о загрузке файла.
     *
     * @param fileMetadata метаданные загруженного файла
     * @param userId ID пользователя, загрузившего файл
     */
    public void publishFileUploadEvent(FileMetadata fileMetadata, Long userId) {
        FileUploadEvent event = FileUploadEvent.builder()
                .fileId(fileMetadata.getId())
                .userId(userId)
                .s3Key(fileMetadata.getUniqueName())
                .bucketName(yandexStorageProperties.getBucketName())
                .originalFileName(fileMetadata.getOriginalName())
                .build();

        fileEventProducer.sendFileUploadEvent(event);

        log.debug("File upload event published: fileId={}, s3Key={}",
                fileMetadata.getId(), fileMetadata.getUniqueName());
    }
}

package org.resume.s3filemanager.service.file;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.resume.s3filemanager.exception.S3YandexException;
import org.resume.s3filemanager.properties.YandexStorageProperties;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;

/**
 * Сервис для работы с объектным хранилищем Yandex Cloud S3.
 * <p>
 * Обеспечивает операции загрузки, скачивания и удаления файлов
 * через AWS SDK v2 с использованием S3-совместимого API.
 *
 * @see S3Client
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class YandexStorageService {

    private final S3Client yandexS3Client;
    private final YandexStorageProperties properties;

    /**
     * Загружает файл в Yandex Object Storage.
     *
     * @param uniqueFileName уникальное имя файла (ключ объекта в S3)
     * @param bytes содержимое файла в виде массива байт
     * @param contentType MIME-тип файла
     * @throws S3YandexException при ошибке взаимодействия с S3
     */
    public void uploadFileYandexS3(String uniqueFileName, byte[] bytes, String contentType) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(properties.getBucketName())
                    .key(uniqueFileName)
                    .contentType(contentType)
                    .contentLength((long) bytes.length)
                    .build();

            yandexS3Client.putObject(request, RequestBody.fromBytes(bytes));

        } catch (S3Exception e) {
            log.error("S3 error uploading file: {}", uniqueFileName, e);
            throw new S3YandexException(e, uniqueFileName);
        }
    }

    /**
     * Скачивает файл из Yandex Object Storage.
     *
     * @param uniqueFileName уникальное имя файла (ключ объекта в S3)
     * @return содержимое файла в виде массива байт
     * @throws S3YandexException при ошибке взаимодействия с S3 или чтения потока
     */
    public byte[] downloadFileYandexS3(String uniqueFileName) {
        try (ResponseInputStream<GetObjectResponse> response = yandexS3Client.getObject(
                GetObjectRequest.builder()
                        .bucket(properties.getBucketName())
                        .key(uniqueFileName)
                        .build()
        )) {
            return response.readAllBytes();

        } catch (S3Exception e) {
            log.error("S3 error downloading file: {}", uniqueFileName, e);
            throw new S3YandexException(e, uniqueFileName);
        } catch (IOException e) {
            log.error("IO error downloading file: {}", uniqueFileName, e);
            throw new S3YandexException(e, uniqueFileName);
        }
    }

    /**
     * Удаляет файл из Yandex Object Storage.
     *
     * @param uniqueFileName уникальное имя файла (ключ объекта в S3)
     * @throws S3YandexException при ошибке взаимодействия с S3
     */
    public void deleteFileYandexS3(String uniqueFileName) {
        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(properties.getBucketName())
                    .key(uniqueFileName)
                    .build();

            yandexS3Client.deleteObject(request);

        } catch (S3Exception e) {
            log.error("S3 error deleting file: {}", uniqueFileName, e);
            throw new S3YandexException(e, uniqueFileName);
        }
    }
}

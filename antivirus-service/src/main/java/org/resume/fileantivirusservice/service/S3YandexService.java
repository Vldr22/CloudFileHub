package org.resume.fileantivirusservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.resume.fileantivirusservice.constant.ErrorMessages;
import org.resume.fileantivirusservice.exception.S3DownloadException;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.resume.common.properties.YandexStorageProperties;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Service
@RequiredArgsConstructor
@EnableConfigurationProperties(YandexStorageProperties.class)
public class S3YandexService {

    private final S3Client yandexS3Client;
    private final YandexStorageProperties yandexStorageProperties;

    /**
     * Скачивает файл из S3 по ключу.
     *
     * @param s3Key уникальный ключ файла в S3
     * @return InputStream с содержимым файла
     * @throws S3DownloadException если произошла техническая ошибка (network, timeout, 500)
     */
    public InputStream downloadFile(String s3Key) {
        String bucketName = yandexStorageProperties.getBucketName();

        log.info("Downloading file from S3: bucket={}, s3Key={}", bucketName, s3Key);

        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            ResponseInputStream<GetObjectResponse> s3Object = yandexS3Client.getObject(getObjectRequest);

            byte[] fileBytes = s3Object.readAllBytes();

            log.info("File downloaded successfully: s3Key={}, size={} bytes", s3Key, fileBytes.length);

            return new ByteArrayInputStream(fileBytes);

        } catch (S3Exception e) {
            log.error("S3 service error while downloading file: s3Key={}, statusCode={}",
                    s3Key, e, e);
            throw new S3DownloadException(ErrorMessages.S3_SERVICE_UNAVAILABLE, e);
        } catch (IOException e) {
            log.error("IO error while reading file from S3: s3Key={}", s3Key, e);
            throw new S3DownloadException(ErrorMessages.S3_READ_FAILED, e);
        }
    }
}

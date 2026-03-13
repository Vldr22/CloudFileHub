package org.resume.s3filemanager.service.file;

import com.github.javafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.resume.common.properties.YandexStorageProperties;
import org.resume.s3filemanager.exception.S3YandexException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class YandexStorageServiceTest {

    private static final Faker FAKER = new Faker();

    @Mock
    private S3Client yandexS3Client;

    @Mock
    private YandexStorageProperties properties;

    @InjectMocks
    private YandexStorageService storageService;

    private String fileName;
    private byte[] content;
    private String contentType;

    @BeforeEach
    void setUp() {
        fileName = FAKER.file().fileName();
        content = FAKER.lorem().sentence().getBytes();
        contentType = "text/plain";

        when(properties.getBucketName()).thenReturn("test-bucket");
    }

    /**
     * Проверяет что при ошибке S3 во время загрузки бросается {@link S3YandexException}.
     */
    @Test
    void shouldThrowS3YandexException_whenUploadFails() {
        when(yandexS3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenThrow(S3Exception.builder().message("S3 error").build());

        assertThatThrownBy(() -> storageService.uploadFileYandexS3(fileName, content, contentType))
                .isInstanceOf(S3YandexException.class);
    }

    /**
     * Проверяет что при ошибке S3 во время скачивания бросается {@link S3YandexException}.
     */
    @Test
    void shouldThrowS3YandexException_whenDownloadFails() {
        when(yandexS3Client.getObject(any(GetObjectRequest.class)))
                .thenThrow(S3Exception.builder().message("S3 error").build());

        assertThatThrownBy(() -> storageService.downloadFileYandexS3(fileName))
                .isInstanceOf(S3YandexException.class);
    }

    /**
     * Проверяет что при ошибке S3 во время удаления бросается {@link S3YandexException}.
     */
    @Test
    void shouldThrowS3YandexException_whenDeleteFails() {
        when(yandexS3Client.deleteObject(any(DeleteObjectRequest.class)))
                .thenThrow(S3Exception.builder().message("S3 error").build());

        assertThatThrownBy(() -> storageService.deleteFileYandexS3(fileName))
                .isInstanceOf(S3YandexException.class);
    }
}
package org.resume.s3filemanager.kafka;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.resume.common.model.FileUploadEvent;
import org.resume.common.properties.YandexStorageProperties;
import org.resume.s3filemanager.entity.FileMetadata;
import org.resume.s3filemanager.entity.User;
import org.resume.s3filemanager.service.kafka.FileEventProducer;
import org.resume.s3filemanager.service.kafka.FileEventService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileEventServiceTest {

    @Mock
    private FileEventProducer fileEventProducer;

    @Mock
    private YandexStorageProperties yandexStorageProperties;

    @InjectMocks
    private FileEventService fileEventService;

    /**
     * Проверяет что событие собирается с правильными полями из FileMetadata.
     */
    @Test
    void shouldPublishEventWithCorrectFields() {
        User user = new User();
        user.setId(10L);

        FileMetadata metadata = FileMetadata.builder()
                .id(42L)
                .uniqueName("test-key.pdf")
                .originalName("document.pdf")
                .user(user)
                .build();

        when(yandexStorageProperties.getBucketName()).thenReturn("test-bucket");

        fileEventService.publishFileUploadEvent(metadata);

        ArgumentCaptor<FileUploadEvent> captor = ArgumentCaptor.forClass(FileUploadEvent.class);
        verify(fileEventProducer).sendFileUploadEvent(captor.capture());

        FileUploadEvent event = captor.getValue();
        assertThat(event.getFileId()).isEqualTo(42L);
        assertThat(event.getUserId()).isEqualTo(10L);
        assertThat(event.getS3Key()).isEqualTo("test-key.pdf");
        assertThat(event.getBucketName()).isEqualTo("test-bucket");
        assertThat(event.getOriginalFileName()).isEqualTo("document.pdf");
    }

    /**
     * Проверяет перегруженный метод — userId извлекается из связанного User.
     */
    @Test
    void shouldExtractUserIdFromEntity_whenUserIdNotProvided() {
        User user = new User();
        user.setId(99L);

        FileMetadata metadata = FileMetadata.builder()
                .id(1L)
                .uniqueName("key.pdf")
                .originalName("file.pdf")
                .user(user)
                .build();

        when(yandexStorageProperties.getBucketName()).thenReturn("bucket");

        fileEventService.publishFileUploadEvent(metadata);

        ArgumentCaptor<FileUploadEvent> captor = ArgumentCaptor.forClass(FileUploadEvent.class);
        verify(fileEventProducer).sendFileUploadEvent(captor.capture());

        assertThat(captor.getValue().getUserId()).isEqualTo(99L);
    }
}
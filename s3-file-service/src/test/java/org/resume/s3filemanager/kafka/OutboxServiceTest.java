package org.resume.s3filemanager.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.resume.common.properties.YandexStorageProperties;
import org.resume.s3filemanager.entity.FileMetadata;
import org.resume.s3filemanager.entity.OutboxEvent;
import org.resume.s3filemanager.entity.User;
import org.resume.s3filemanager.enums.OutboxEventType;
import org.resume.s3filemanager.enums.OutboxStatus;
import org.resume.s3filemanager.exception.OutboxSerializationException;
import org.resume.s3filemanager.properties.OutboxProperties;
import org.resume.s3filemanager.repository.OutboxRepository;
import org.resume.s3filemanager.service.kafka.OutboxEventProcessor;
import org.resume.s3filemanager.service.kafka.OutboxService;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxServiceTest {

    private static final Faker FAKER = new Faker();

    @Mock
    private OutboxRepository outboxRepository;

    @Mock
    private OutboxEventProcessor outboxEventProcessor;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private OutboxProperties outboxProperties;

    @Mock
    private YandexStorageProperties yandexStorageProperties;

    @InjectMocks
    private OutboxService outboxService;

    private FileMetadata fileMetadata;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(FAKER.number().randomNumber());
        user.setUsername(FAKER.name().username());

        fileMetadata = FileMetadata.builder()
                .id(FAKER.number().randomNumber())
                .uniqueName(FAKER.internet().uuid() + ".pdf")
                .originalName(FAKER.file().fileName(null, null, "pdf", null))
                .type("application/pdf")
                .size(FAKER.number().numberBetween(1L, 10_000_000L))
                .fileHash(FAKER.internet().uuid())
                .user(user)
                .build();
    }

    /**
     * Успешное сохранение события — событие сериализуется и сохраняется в Outbox.
     */
    @Test
    void shouldSaveOutboxEvent_whenFileUploadEventIsValid() throws JsonProcessingException {
        String payload = "{\"fileId\":1}";
        when(yandexStorageProperties.getBucketName()).thenReturn("test-bucket");
        when(objectMapper.writeValueAsString(any())).thenReturn(payload);

        outboxService.saveFileUploadEvent(fileMetadata, user.getId());

        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxRepository).save(captor.capture());

        OutboxEvent saved = captor.getValue();
        assertThat(saved.getEventType()).isEqualTo(OutboxEventType.FILE_UPLOAD);
        assertThat(saved.getPayload()).isEqualTo(payload);
        assertThat(saved.getStatus()).isEqualTo(OutboxStatus.PENDING);
        assertThat(saved.getRetryCount()).isEqualTo(0);
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getNextRetryAt()).isNotNull();
    }

    /**
     * Ошибка сериализации — бросается OutboxSerializationException.
     */
    @Test
    void shouldThrowOutboxSerializationException_whenSerializationFails() throws JsonProcessingException {
        when(yandexStorageProperties.getBucketName()).thenReturn("test-bucket");
        when(objectMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("error") {});

        assertThatThrownBy(() -> outboxService.saveFileUploadEvent(fileMetadata, user.getId()))
                .isInstanceOf(OutboxSerializationException.class);

        verify(outboxRepository, never()).save(any());
    }

    /**
     * Нет PENDING событий — processor не вызывается.
     */
    @Test
    void shouldNotProcess_whenNoPendingEvents() {
        when(outboxProperties.getBatchSize()).thenReturn(10);
        when(outboxRepository.findPendingEvents(eq(OutboxStatus.PENDING), any(Instant.class), any(PageRequest.class)))
                .thenReturn(List.of());

        outboxService.processPendingEvents();

        verifyNoInteractions(outboxEventProcessor);
    }

    /**
     * Есть PENDING события — processor вызывается для каждого.
     */
    @Test
    void shouldProcessEachEvent_whenPendingEventsExist() {
        OutboxEvent event1 = mock(OutboxEvent.class);
        OutboxEvent event2 = mock(OutboxEvent.class);

        when(outboxProperties.getBatchSize()).thenReturn(10);
        when(outboxRepository.findPendingEvents(eq(OutboxStatus.PENDING), any(Instant.class), any(PageRequest.class)))
                .thenReturn(List.of(event1, event2));

        outboxService.processPendingEvents();

        verify(outboxEventProcessor).process(event1);
        verify(outboxEventProcessor).process(event2);
    }

    /**
     * Батч размер передаётся корректно в запрос.
     */
    @Test
    void shouldUseBatchSize_fromProperties() {
        int batchSize = 5;
        when(outboxProperties.getBatchSize()).thenReturn(batchSize);
        when(outboxRepository.findPendingEvents(any(), any(), any())).thenReturn(List.of());

        outboxService.processPendingEvents();

        verify(outboxRepository).findPendingEvents(
                eq(OutboxStatus.PENDING),
                any(Instant.class),
                eq(PageRequest.of(0, batchSize))
        );
    }
}